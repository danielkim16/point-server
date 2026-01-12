package com.musinsa.point.api.infrastructure.persistence.repository;

import com.musinsa.data.point.infrastructure.persistence.repository.PointLedgerJpaRepository;
import com.musinsa.data.point.model.PointEventType;
import com.musinsa.data.point.model.PointLedger;

import java.util.List;

public interface PointLedgerRepository extends PointLedgerJpaRepository {
    /**
     * 특정 거래번호(주문번호)로 발생한 포인 사용 내역 조회
     */
    List<PointLedger> findByTradeNoAndEventType(String tradeNo, PointEventType eventType);

    /**
     * 특정 거래번호(주문번호)의 특정 이벤트 타입들 조회
     */
    List<PointLedger> findByTradeNoAndEventTypeIn(String tradeNo, List<PointEventType> eventTypes);
}
