package com.musinsa.point.api.infrastructure.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.musinsa.point.api.presentation..*(..))")
    public void logRequest(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            logRequestHeaders(wrapper);
            logRequestBody(wrapper);
        } else {
            log.warn("Request is not an instance of ContentCachingRequestWrapper.");
        }
    }

    private void logRequestHeaders(HttpServletRequest request) {
        log.info("Request: {} {}" + request.getMethod(), request.getRequestURI());

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("Request Header: {}={}", headerName, request.getHeader(headerName));
        }

        log.info("Request Parameters:");
        request.getParameterMap().forEach((key, value) -> log.info("{}={}", key, String.join(",", value)));
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            String payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
            log.info("Request Body: {}", payload);
        }
    }
}
