package com.musinsa.data.point.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "POINT_LEDGER",
        indexes = {
                @Index(name = "idx_ledger_trade_no", columnList = "tradeNo"),
                @Index(name = "idx_ledger_member_id_created_at", columnList = "memberId, createdAt")
        }
)
public class PointLedger extends CreatedDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("회원 ID")
    @Column(nullable = false)
    private Long memberId;

    @Comment("연관된 포인트 적립 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "earning_id", nullable = false)
    private PointEarning pointEarning;

    @Comment("거래 번호 (주문번호 또는 트랜잭션 ID)")
    @Column(length = 100)
    private String tradeNo;

    @Comment("변동 금액 (양수: 적립/취소복구, 음수: 사용)")
    @Column(nullable = false)
    private Long amount;

    @Comment("이벤트 유형")
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PointEventType eventType;

    @Comment("사유")
    private String description;

    @Builder
    public PointLedger(Long memberId, PointEarning pointEarning, String tradeNo, Long amount, PointEventType eventType, String description) {
        this.memberId = memberId;
        this.pointEarning = pointEarning;
        this.tradeNo = tradeNo;
        this.amount = amount;
        this.eventType = eventType;
        this.description = description;
    }
}