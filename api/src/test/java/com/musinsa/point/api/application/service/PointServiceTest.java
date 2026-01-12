package com.musinsa.point.api.application.service;

import com.musinsa.data.point.model.*;
import com.musinsa.point.api.infrastructure.persistence.repository.PointConfigRepository;
import com.musinsa.point.api.infrastructure.persistence.repository.PointEarningRepository;
import com.musinsa.point.api.infrastructure.persistence.repository.PointLedgerRepository;
import com.musinsa.point.api.presentation.PointType;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointEarningRepository pointEarningRepository;
    @Mock
    private PointLedgerRepository pointLedgerRepository;
    @Mock
    private PointConfigRepository pointConfigRepository;

    private static final Long MEMBER_ID = 1L;
    private static final String POINT_KEY = "ORD_20260111_1";

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 포인트_적립_테스트 {

        @Test
        void 적립_요청_시_포인트가_저장되고_원장이_기록되어야_한다() {
            EarnPointCommand command = new EarnPointCommand(MEMBER_ID, 1000L, PointType.PURCHASE_REWARD, POINT_KEY, 365, "구매 적립");

            // 중복 체크 통과
            given(pointEarningRepository.existsByPointKey(command.pointKey())).willReturn(false);

            // 설정값 조회 (1회 적립 한도, 최대 보유 한도)
            mockConfig(PointConfigType.MAX_EARN_PER_ONCE, 100_000L);
            mockConfig(PointConfigType.MAX_BALANCE_LIMIT, 2_000_000L);

            // 현재 잔액 조회 (0원 가정)
            given(pointEarningRepository.sumValidBalance(eq(MEMBER_ID), any(LocalDateTime.class))).willReturn(0L);

            // 포인트 적립하기
            EarnPointInfo info = pointService.earn(command);

            // 1000포인트 적립 확인하기
            assertThat(info.totalBalance()).isEqualTo(1000L);

            verify(pointEarningRepository, times(1)).save(any(PointEarning.class));
            verify(pointLedgerRepository, times(1)).save(any(PointLedger.class));
        }

        @Test
        void 이미_처리된_pointKey인_경우_예외가_발생해야_한다() {
            EarnPointCommand command = new EarnPointCommand(MEMBER_ID, 1000L, PointType.PURCHASE_REWARD, POINT_KEY, 365, "중복 적립 시도");

            given(pointEarningRepository.existsByPointKey(command.pointKey())).willReturn(true);

            assertThatThrownBy(() -> pointService.earn(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 처리된 포인트 적립 요청입니다");
        }

        @Test
        void 최대_적립_한도를_초과하는_경우_예외가_발생해야_한다() {
            EarnPointCommand command = new EarnPointCommand(MEMBER_ID, 2000L, PointType.PURCHASE_REWARD, POINT_KEY, 365, "1회 직립 포인트 초과");

            given(pointEarningRepository.existsByPointKey(command.pointKey())).willReturn(false);
            mockConfig(PointConfigType.MAX_EARN_PER_ONCE, 1000L);

            assertThatThrownBy(() -> pointService.earn(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1회 최대 적립 한도");
        }

        @Test
        void 최대_보유_한도를_초과하는_경우_예외가_발생해야_한다() {
            EarnPointCommand command = new EarnPointCommand(MEMBER_ID, 1000L, PointType.PURCHASE_REWARD, POINT_KEY, 365, "한도 초과");

            given(pointEarningRepository.existsByPointKey(command.pointKey())).willReturn(false);
            mockConfig(PointConfigType.MAX_EARN_PER_ONCE, 5000L);
            mockConfig(PointConfigType.MAX_BALANCE_LIMIT, 5000L);

            // 현재 4500원을 가지고 있다고 가정
            given(pointEarningRepository.sumValidBalance(eq(MEMBER_ID), any(LocalDateTime.class))).willReturn(4500L);

            // 4500 + 1000 > 5000 으로 개인별 최대 보유 한도를 초과
            assertThatThrownBy(() -> pointService.earn(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("개인별 최대 보유 한도");
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 포인트_사용_테스트 {

        @Test
        void 여러_건의_포인트_상세에서_순차적으로_차감되어야_한다() {
            Long useAmount = 1200L;

            PointEarning p1 = createPointEarning(1000L);
            PointEarning p2 = createPointEarning(500L);

            given(pointEarningRepository.findAvailablePoints(eq(MEMBER_ID), any(LocalDateTime.class)))
                    .willReturn(List.of(p1, p2));

            pointService.use(MEMBER_ID, useAmount, "ORDER_001", "상품 구매 포인트 적립");

            // 1000포인트에서 전액 사용해서 0포인트 남음
            assertThat(p1.getBalanceAmount()).isEqualTo(0L);
            // 500포인트에서 200포인트 사용해서 300포인트 남음
            assertThat(p2.getBalanceAmount()).isEqualTo(300L);

            // 원장 기록 확인
            verify(pointLedgerRepository, times(2)).save(any(PointLedger.class));
        }

        @Test
        void 보유_잔액보다_사용_요청_금액이_큰_경우_예외가_발생해야_한다() {
            Long useAmount = 3000L;
            PointEarning p1 = createPointEarning(1000L);

            given(pointEarningRepository.findAvailablePoints(eq(MEMBER_ID), any(LocalDateTime.class)))
                    .willReturn(List.of(p1));

            assertThatThrownBy(() -> pointService.use(MEMBER_ID, useAmount, "ORDER_1", "포인트 초과 사용"))
                    .isInstanceOf(NotEnoughPointException.class);
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 포인트_사용_취소_테스트 {

        @Test
        void 만료된_포인트_취소_시_신규_적립_즉_재발급_처리되어야_한다() {
            String orderNo = "ORDER_1";

            // 만료된 원본 포인트
            PointEarning originExpired = PointEarning.builder()
                    .memberId(MEMBER_ID)
                    .originalAmount(1000L)
                    .balanceAmount(0L)
                    .expireAt(LocalDateTime.now().minusDays(1))
                    .build();

            PointLedger usageHistory = PointLedger.builder()
                    .pointEarning(originExpired)
                    .amount(-1000L)
                    .tradeNo(orderNo)
                    .eventType(PointEventType.USE)
                    .build();

            given(pointLedgerRepository.findByTradeNoAndEventType(orderNo, PointEventType.USE))
                    .willReturn(List.of(usageHistory));

            // 환불 사전 검증 통과하려면 기 환불된 내역이 없어야 함
            given(pointLedgerRepository.findByTradeNoAndEventTypeIn(eq(orderNo), anyList()))
                    .willReturn(List.of());

            pointService.refund(MEMBER_ID, orderNo, 1000L, "사용된 포인트 환불");

            // 재발급 로직이 수행되어 save()가 호출되었는지 확인
            verify(pointEarningRepository, times(1)).save(argThat(info -> {
                return info.getPointKey().startsWith("REINSTATE_") &&
                        info.getOriginalAmount() == 1000L;
            }));

            // 원장 기록 확인
            verify(pointLedgerRepository, times(1)).save(argThat(ledger ->
                    ledger.getEventType() == PointEventType.EXPIRE_REINSTATE
            ));
        }

        @Test
        void 유효한_포인트_취소_시_잔액이_복구되어야_한다() {
            String orderNo = "ORDER_1";

            // 아직 만료되지 않은 포인트
            PointEarning originValid = PointEarning.builder()
                    .memberId(MEMBER_ID)
                    .originalAmount(1000L)
                    .balanceAmount(500L) // 500원 사용된 상태
                    .expireAt(LocalDateTime.now().plusDays(30))
                    .build();

            PointLedger usageHistory = PointLedger.builder()
                    .pointEarning(originValid)
                    .amount(-500L)
                    .tradeNo(orderNo)
                    .eventType(PointEventType.USE)
                    .build();

            // 원장에서 사용한 -500 포인트 조회
            given(pointLedgerRepository.findByTradeNoAndEventType(orderNo, PointEventType.USE))
                    .willReturn(List.of(usageHistory));

            // 환불 사전 검증 통과하려면 기 환불된 내역이 없어야 함
            given(pointLedgerRepository.findByTradeNoAndEventTypeIn(eq(orderNo), anyList()))
                    .willReturn(List.of());

            pointService.refund(MEMBER_ID, orderNo, 500L, "포인트 사용 취소");

            // 잔액 복구 확인 (500 -> 1000)
            assertThat(originValid.getBalanceAmount()).isEqualTo(1000L);

            // 원장 기록 확인
            verify(pointLedgerRepository, times(1)).save(argThat(ledger ->
                    ledger.getEventType() == PointEventType.REFUND
            ));
        }
    }

    private void mockConfig(PointConfigType type, Long value) {
        PointConfig config = mock(PointConfig.class);
        given(config.getValueAsLong()).willReturn(value);
        given(pointConfigRepository.findByConfigKey(type.name())).willReturn(Optional.of(config));
    }

    private PointEarning createPointEarning(Long amount) {
        return PointEarning.builder()
                .memberId(MEMBER_ID)
                .originalAmount(amount)
                .balanceAmount(amount)
                .expireAt(LocalDateTime.now().plusDays(365))
                .build();
    }
}