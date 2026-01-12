package com.musinsa.point.api.application.service;

import com.musinsa.point.api.presentation.PointType;

public record EarnPointCommand(
        Long memberId,      // PathVariable에서 받은 ID가 우선됨
        Long amount,
        PointType type,
        String pointKey,
        Integer expireDays,
        String description
) {
}
