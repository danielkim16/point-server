package com.musinsa.point.api.application.service;

import com.musinsa.data.point.model.PointEarning;

import java.time.LocalDateTime;

public record EarnPointInfo(
        Long earningId,
        Long memberId,
        Long totalBalance, // 적립 후 총 잔액
        String pointKey,
        LocalDateTime expireAt
) {
    public static EarnPointInfo from(PointEarning entity) {
        return new EarnPointInfo(
                entity.getId(),
                entity.getMemberId(),
                entity.getBalanceAmount(),
                entity.getPointKey(),
                entity.getExpireAt()
        );
    }
}
