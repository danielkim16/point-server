package com.musinsa.data.point.model;

public enum PointEventType {
    EARN,           // 적립
    REVOKE,
    USE,            // 사용
    REFUND,     // 사용 취소 (기간 내 복구)
    EXPIRE_REINSTATE  // 만료된 포인트 재적립 (취소 시)
}
