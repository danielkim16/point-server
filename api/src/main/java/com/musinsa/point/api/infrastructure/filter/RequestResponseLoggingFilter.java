package com.musinsa.point.api.infrastructure.filter;

import com.musinsa.point.common.http.Headers;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    private final int responseLogMaxLength;

    public RequestResponseLoggingFilter(@Value("${logging.response-log-max-length:0}") int responseLogMaxLength) {
        this.responseLogMaxLength = responseLogMaxLength;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Transaction ID 처리
        String uuid = request.getHeader(Headers.TRANSACTION_ID);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }

        MDC.put(Headers.TRANSACTION_ID, uuid);
        request.setAttribute(Headers.TRANSACTION_ID, uuid);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logResponse(wrappedResponse);
            MDC.remove(Headers.TRANSACTION_ID);

            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        StringBuilder msg = new StringBuilder();
        msg.append("Response Status: ").append(response.getStatus());

        byte[] buf = response.getContentAsByteArray();
        if (buf.length > 0) {
            String payload = new String(buf, StandardCharsets.UTF_8);
            String shortPayload;

            // 길이 제한 로직 적용
            if (responseLogMaxLength > 0 && payload.length() > responseLogMaxLength) {
                shortPayload = payload.substring(0, responseLogMaxLength) + "...";
            } else {
                shortPayload = payload;
            }
            msg.append("\nResponse Payload: ").append(shortPayload);
        }

        log.info(msg.toString());
    }
}