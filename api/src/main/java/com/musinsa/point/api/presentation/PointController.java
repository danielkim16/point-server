package com.musinsa.point.api.presentation;

import com.musinsa.point.api.application.service.EarnPointInfo;
import com.musinsa.point.api.application.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;

    /**
     * 포인트를 적립 처리한다.
     */
    @PostMapping(value = "/earnings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EarnPointResponse> earnPoints(
            @Valid @RequestBody EarnPointRequest request
    ) {
        EarnPointInfo info = pointService.earn(request.toCommand());

        return ResponseEntity.ok(EarnPointResponse.from(info));
    }

    /**
     * 적립한 포인트를 취소 처리한다.
     */
    @PostMapping(value = "/earnings/{earningId}/revoke", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> revokePoints(
            @Valid @RequestBody RevokePointRequest request
    ) {
        pointService.revoke(request.memberId(), request.pointKey(), request.description());
        return ResponseEntity.noContent().build();
    }

    /**
     * 포인트를 사용 처리한다.
     */
    @PostMapping(value = "/usages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> usePoints(
            @Valid @RequestBody UsePointRequest request
    ) {
        pointService.use(
                request.memberId(), request.amount(), request.orderNo(), request.description()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * 사용한 포인트를 환불 처리한다.
     */
    @PostMapping(value = "/refunds", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> refundPoints(
            @Valid @RequestBody RefundPointRequest request
    ) {
        pointService.refund(
                request.memberId(), request.orderNo(), request.amount(), request.description()
        );

        return ResponseEntity.noContent().build();
    }
}
