package com.musinsa.point.api.application.service;

import com.musinsa.data.point.model.*;
import com.musinsa.point.api.infrastructure.persistence.repository.PointConfigRepository;
import com.musinsa.point.api.infrastructure.persistence.repository.PointEarningRepository;
import com.musinsa.point.api.infrastructure.persistence.repository.PointLedgerRepository;
import com.musinsa.point.api.presentation.PointType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointEarningRepository pointDetailRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointConfigRepository pointConfigRepository;

    /**
     * 포인트 적립
     */
    @Transactional
    public EarnPointInfo earn(EarnPointCommand command) {
        // 1. 중복 적립 요청 체크
        if (pointDetailRepository.existsByPointKey(command.pointKey())) {
            throw new IllegalArgumentException("이미 처리된 포인트 적립 요청입니다. (pointKey 중복: " + command.pointKey() + ")");
        }

        // 2. 1회 적립 한도 체크
        validateEarningLimit(command.amount());

        // 3. 개인별 최대 보유 한도 체크
        validateBalanceLimit(command.memberId(), command.amount());

        // 4. 만료일 설정
        int expireDays = (command.expireDays() != null) ? command.expireDays() : 365;
        LocalDateTime expireAt = LocalDateTime.now().plusDays(expireDays);

        // 5. 포인트 상세 생성 및 저장
        PointEarning pointEarning = PointEarning.builder()
                .memberId(command.memberId())
                .pointKey(command.pointKey())
                .originalAmount(command.amount())
                .balanceAmount(command.amount())
                .isManual(command.type() == PointType.ADMIN_MANUAL)
                .expireAt(expireAt)
                .build();

        try {
            pointDetailRepository.save(pointEarning);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 처리된 적립 요청입니다. (pointKey 중복: " + command.pointKey() + ")");
        }

        // 6. 원장 저장
        addLedger(command.memberId(), pointEarning, command.pointKey(), command.amount(), PointEventType.EARN, command.description());

        return EarnPointInfo.from(pointEarning);
    }

    /**
     * 포인트 적립 건을 회수한다.
     */
    @Transactional
    public void revoke(Long memberId, String pointKey, String description) {
        // 1. 회수 대상 포인트 조회
        PointEarning targetPoint = pointDetailRepository.findByPointKey(pointKey)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 포인트 키입니다: " + pointKey));

        // 2. 포인트 소유자 검증
        if (!targetPoint.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("해당 회원의 포인트가 아닙니다.");
        }

        // 3. 회수 가능 잔액 확인
        long revokeAmount = targetPoint.getBalanceAmount();

        // 4. 원 적립 포인트 확인
        long originalAmount = targetPoint.getOriginalAmount();

        // 5. 회수 가능 잔액과 원 적립 포인트가 일치하지 않으면 회수 불가
        if (revokeAmount != originalAmount) {
            throw new IllegalArgumentException("이미 포인트를 사용해서 회수 처리를 할 수 없습니다.");
        }

        // 6. 포인트 차감 (남은 잔액 0으로 만들기)
        targetPoint.use(revokeAmount);

        // 7. 원장 기록
        addLedger(memberId, targetPoint, pointKey, -revokeAmount, PointEventType.REVOKE, description);
    }

    /**
     * 포인트 사용 처리한다.
     * - 낙관적 락 충돌 시 최대 3번, 0.1초 간격으로 재시도
     * - 수기 지급 -> 만료 임박 순으로 차감
     */
    @Retryable(
            retryFor = { ObjectOptimisticLockingFailureException.class },
            exclude = { NotEnoughPointException.class, IllegalArgumentException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void use(Long memberId, Long amount, String orderNo, String description) {
        if (amount <= 0) throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");

        // 1. 사용 가능한 포인트 조회
        List<PointEarning> pointEarnings = pointDetailRepository.findAvailablePoints(memberId, LocalDateTime.now());

        // 2. 보유 잔액 체크
        long totalBalance = pointEarnings.stream().mapToLong(PointEarning::getBalanceAmount).sum();
        if (totalBalance < amount) {
            throw new NotEnoughPointException("잔액이 부족합니다. (보유: " + totalBalance + ")");
        }

        // 3. 차감 로직 수행
        long remainingAmount = amount;

        for (PointEarning pointEarning : pointEarnings) {
            if (remainingAmount == 0) break;

            long deductAmount = Math.min(pointEarning.getBalanceAmount(), remainingAmount);

            // 3-1. 포인트 업데이트
            pointEarning.use(deductAmount);

            // 3-2. 원장 기록
            addLedger(memberId, pointEarning, orderNo, -deductAmount, PointEventType.USE, description);

            remainingAmount -= deductAmount;
        }
    }

    @Recover
    public void recover(ObjectOptimisticLockingFailureException e, Long memberId, Long amount, String orderNo, String description) {
        throw new IllegalArgumentException("시스템 부하로 인해 포인트 사용에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    /**
     * 사용된 포인트를 취소한다.
     * - 만료된 포인트는 신규 적립(재발급) 처리
     */
    @Transactional
    public void refund(Long memberId, String orderNo, Long cancelAmount, String description) {
        if (cancelAmount <= 0) throw new IllegalArgumentException("취소 포인트는 0보다 커야 합니다.");

        // 1. 해당 주문으로 사용된 원장 내역 조회
        List<PointLedger> useHistories = pointLedgerRepository.findByTradeNoAndEventType(orderNo, PointEventType.USE);
        if (useHistories.isEmpty()) {
            throw new IllegalArgumentException("사용 내역이 존재하지 않는 거래번호입니다.");
        }

        // 2. 취소 가능 금액 검증
        // 2-1. 사용했던 포인트 합계
        long totalUsedAmount = useHistories.stream()
                .mapToLong(h -> Math.abs(h.getAmount()))
                .sum();

        // 2-2. 포인트 환불 유형 생성
        List<PointEventType> refundTypes = List.of(PointEventType.REFUND, PointEventType.EXPIRE_REINSTATE);

        // 2-3. 포인트 환불 이력 조회
        List<PointLedger> alreadyRefundedHistories = pointLedgerRepository.findByTradeNoAndEventTypeIn(orderNo, refundTypes);

        long totalRefundedAmount = alreadyRefundedHistories.stream()
                .mapToLong(PointLedger::getAmount)
                .sum();

        // 2-4. 남은 환불 가능 금액 계산
        long refundableAmount = totalUsedAmount - totalRefundedAmount;

        // 2-5. 취소 요청 포인트 환불 가능한 금액인지 검증
        if (cancelAmount > refundableAmount) {
            throw new IllegalArgumentException(
                    "취소 요청 포인트가 취소 가능 잔액을 초과했습니다. " +
                            "(총 사용 포인트: " + totalUsedAmount +
                            ", 기 취소 포인트: " + totalRefundedAmount +
                            ", 취소 요청 포인트: " + cancelAmount + ")"
            );
        }

        long remainingCancelAmount = cancelAmount;

        // 3. 취소 처리
        for (PointLedger history : useHistories) {
            if (remainingCancelAmount == 0) break;

            long usedInHistory = Math.abs(history.getAmount());
            long restorePoint = Math.min(usedInHistory, remainingCancelAmount);

            PointEarning originPoint = history.getPointEarning();

            // 4. 만료 여부 체크
            if (originPoint.isExpired()) {
                // 만료됨 -> 신규 적립 (재발급)
                reinstateExpiredPoint(memberId, restorePoint, originPoint, orderNo);
            } else {
                // 유효함 -> 잔액 복구
                originPoint.restore(restorePoint);
                addLedger(memberId, originPoint, orderNo, restorePoint, PointEventType.REFUND, description);
            }

            remainingCancelAmount -= restorePoint;
        }

        if (remainingCancelAmount > 0) {
            throw new IllegalArgumentException("취소 요청 포인트가 사용된 포인트 보다 큽니다.");
        }
    }

    /**
     * 만료된 포인트 재발급 처리한다.
     */
    private void reinstateExpiredPoint(Long memberId, Long amount, PointEarning originPointEarningInfo, String originOrderNo) {
        // 재발급 포인트 유효기간 설정 (기본 365일)
        LocalDateTime newExpireAt = LocalDateTime.now().plusDays(365);

        PointEarning reissuedPoint = PointEarning.builder()
                .memberId(memberId)
                .pointKey("REINSTATE_" + originOrderNo + "_" + originPointEarningInfo.getId()) // 유니크 키 생성 전략
                .originalAmount(amount)
                .balanceAmount(amount)
                .isManual(originPointEarningInfo.isManual()) // 원본 속성 승계
                .expireAt(newExpireAt)
                .build();

        pointDetailRepository.save(reissuedPoint);

        addLedger(memberId, reissuedPoint, originOrderNo, amount, PointEventType.EXPIRE_REINSTATE, "만료된 포인트 사용 취소 처리에 따른 재적립");
    }

    /**
     * 포인트 원장에 히스토리를 등록 처리한다.
     */
    private void addLedger(Long memberId, PointEarning pointEarning, String tradeNo, Long point, PointEventType type, String desc) {
        PointLedger ledger = PointLedger.builder()
                .memberId(memberId)
                .pointEarning(pointEarning)
                .tradeNo(tradeNo)
                .amount(point)
                .eventType(type)
                .description(desc)
                .build();
        pointLedgerRepository.save(ledger);
    }

    /**
     * 포인트 1회 적립 한도를 검증한다.
     */
    private void validateEarningLimit(Long point) {
        // 1. 최소 조건 1포인트 이상인지 체크
        if (point < 1) {
            throw new IllegalArgumentException("포인트 적립은 최소 1포인트 이상이어야 합니다.");
        }

        // 2. 최대 조건 DB 설정값 조회
        PointConfig config = pointConfigRepository.findByConfigKey(PointConfigType.MAX_EARN_PER_ONCE.name())
                .orElseThrow(() -> new IllegalStateException("시스템 설정 오류: 필수 설정값(MAX_EARN_PER_ONCE)이 누락되었습니다."));

        long limit = config.getValueAsLong();

        // 3. 한도 초과 체크
        if (point > limit) {
            throw new IllegalArgumentException("1회 최대 적립 한도(" + limit + " 포인트)를 초과했습니다.");
        }
    }

    /**
     * 개인별 최대 보유 한도를 검증한다.
     */
    private void validateBalanceLimit(Long memberId, Long newAmount) {
        // 1. 설정값 조회
        PointConfig config = pointConfigRepository.findByConfigKey(PointConfigType.MAX_BALANCE_LIMIT.name())
                .orElseThrow(() -> new IllegalStateException("시스템 설정 오류: 필수 설정값(MAX_BALANCE_LIMIT)이 누락되었습니다."));

        long maxLimit = config.getValueAsLong();

        // 2. 현재 회원의 유효한(만료안된) 총 잔액 조회
        Long currentBalance = pointDetailRepository.sumValidBalance(memberId, LocalDateTime.now());

        // 3. 한도 초과 계산
        if (currentBalance + newAmount > maxLimit) {
            throw new IllegalArgumentException(
                    "개인별 최대 보유 한도(" + maxLimit + ")를 초과할 수 없습니다. " +
                            "(현재: " + currentBalance + ", 요청: " + newAmount + ")");
        }
    }
}