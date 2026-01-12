package com.musinsa.data.point.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "POINT_EARNING",
        indexes = {
                @Index(name = "idx_member_id_expire_at", columnList = "memberId, expireAt")
        }
)
public class PointEarning extends DateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("회원 ID")
    @Column(nullable = false)
    private Long memberId;

    @Comment("적립 고유 키 (중복 방지용)")
    @Column(nullable = false, unique = true, length = 100)
    private String pointKey;

    @Comment("최초 적립 금액")
    @Column(nullable = false)
    private Long originalAmount;

    @Comment("현재 사용 가능 잔액")
    @Column(nullable = false)
    private Long balanceAmount;

    @Comment("관리자 수기 지급 여부")
    @Column(nullable = false)
    private boolean isManual;

    @Comment("만료 일시")
    @Column(nullable = false)
    private LocalDateTime expireAt;

    @Builder
    public PointEarning(Long memberId, String pointKey, Long originalAmount, Long balanceAmount, boolean isManual, LocalDateTime expireAt) {
        this.memberId = memberId;
        this.pointKey = pointKey;
        this.originalAmount = originalAmount;
        this.balanceAmount = balanceAmount;
        this.isManual = isManual;
        this.expireAt = expireAt;
    }

    /**
     * 포인트 사용 처리하기
     * @param amount 사용하려는 포인트
     */
    public void use(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("포인트는 0보다 커야 합니다.");
        }
        if (this.balanceAmount < amount) {
            throw new NotEnoughPointException("잔액이 부족합니다.");
        }
        this.balanceAmount -= amount;
    }

    /**
     * 사용된 포인트를 복구한다.
     * @param amount 환불 포인트
     */
    public void restore(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("포인트는 0보다 커야 합니다.");
        }
        if (this.balanceAmount + amount > this.originalAmount) {
            throw new IllegalStateException("잔액이 최초 적립 금액을 초과할 수 없습니다.");
        }
        this.balanceAmount += amount;
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expireAt);
    }

    /**
     * 현재 사용 가능한 상태인지 확인
     */
    public boolean isAvailable() {
        return !isExpired() && this.balanceAmount > 0;
    }
}