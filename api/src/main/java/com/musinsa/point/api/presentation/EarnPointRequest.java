package com.musinsa.point.api.presentation;

import com.musinsa.point.api.application.service.EarnPointCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record EarnPointRequest(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotNull(message = "적립 포인트는 필수입니다.")
        @Min(value = 1, message = "적립 포인트는 1원 이상이어야 합니다.")
        Long amount,

        @NotNull(message = "포인트 적립 유형은 필수입니다.")
        PointType type, // PURCHASE_REWARD(구매 보상), ADMIN_MANUAL(수기)

        @NotBlank(message = "적립 고유 키(pointKey)는 필수입니다.")
        String pointKey, // 중복 적립 방지용 식별자

        Integer expireDays, // 만료 기간(일). Null이면 시스템 기본 설정값(예: 365일) 사용

        String description // 적립 사유 (옵션)
) implements Serializable {
    public EarnPointCommand toCommand() {
        return new EarnPointCommand(
                this.memberId,
                this.amount,
                this.type,
                this.pointKey,
                this.expireDays,
                this.description
        );
    }
}
