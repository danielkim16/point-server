package com.musinsa.point.api.presentation;

import com.musinsa.point.common.http.PointProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ApiErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ProblemDetail> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String message = (String) request.getAttribute("javax.servlet.error.message");

        return new ResponseEntity<>(
                PointProblemDetail.forStatusAndDetailAndProperties(
                        HttpStatusCode.valueOf(statusCode),
                        message,
                        Map.of("error", HttpStatus.INTERNAL_SERVER_ERROR.name())
                ),
                HttpStatusCode.valueOf(statusCode));
    }
}
