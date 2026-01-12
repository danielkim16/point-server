package com.musinsa.point.api.presentation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.musinsa.point.api.application.service.EarnPointInfo;

import java.time.LocalDateTime;

public record EarnPointResponse(
        Long earningId, // 포인트 적립 ID
        Long memberId, // 회원 ID
        Long totalBalance, // 적립 후 잔액
        String pointKey, // 요청했던 고유 키

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expireAt // 만료 일시
) {
    public static EarnPointResponse from(EarnPointInfo info) {
        return new EarnPointResponse(
                info.earningId(),
                info.memberId(),
                info.totalBalance(),
                info.pointKey(),
                info.expireAt()
        );
    }
}
