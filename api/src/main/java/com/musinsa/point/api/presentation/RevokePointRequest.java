package com.musinsa.point.api.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record RevokePointRequest(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotBlank(message = "적립 고유 키(pointKey)는 필수입니다.")
        String pointKey, // 중복 적립 방지용 식별자

        String description // 적립 취소 사유 (옵션)
) implements Serializable {
}
