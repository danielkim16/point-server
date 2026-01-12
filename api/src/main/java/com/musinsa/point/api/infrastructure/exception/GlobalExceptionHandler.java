package com.musinsa.point.api.infrastructure.exception;

import com.musinsa.data.point.model.NotEnoughPointException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    @ExceptionHandler(NotEnoughPointException.class)
    public ProblemDetail handleNotEnoughPointException(NotEnoughPointException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ExhaustedRetryException.class)
    public ProblemDetail handleExhaustedRetry(ExhaustedRetryException e) {
        if (e.getCause() instanceof NotEnoughPointException) {
            return handleNotEnoughPointException((NotEnoughPointException) e.getCause());
        }

        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[{}] 잘못된 요청: {}", request.getRequestURI(), e.getMessage());

        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e, HttpServletRequest request) {
        log.error("[{}] 요청을 처리하는 도중에 에러가 발생했습니다.", request.getRequestURI(), e);

        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(null);
    }
}
