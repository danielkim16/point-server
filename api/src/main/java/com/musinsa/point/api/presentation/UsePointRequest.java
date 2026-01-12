package com.musinsa.point.api.presentation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record UsePointRequest(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotNull(message = "주문번호는 필수입니다.")
        String orderNo,

        @NotNull(message = "사용 포인트는 필수입니다.")
        @Min(value = 1, message = "사용 포인트는 1원 이상이어야 합니다.")
        Long amount,

        String description
) implements Serializable {
}
