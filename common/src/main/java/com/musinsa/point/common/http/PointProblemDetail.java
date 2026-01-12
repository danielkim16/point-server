package com.musinsa.point.common.http;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Map;

public class PointProblemDetail extends ProblemDetail {
    public static ProblemDetail forStatusAndDetailAndProperties(HttpStatusCode status, @Nullable String detail, Map<String, Object> properties) {
        Assert.notNull(status, "HttpStatusCode is required");
        ProblemDetail problemDetail = forStatus(status.value());
        problemDetail.setDetail(detail);
        problemDetail.setProperties(properties);
        return problemDetail;
    }

    public static ProblemDetail forStatusAndDetailAndUriAndProperties(
            HttpStatusCode status, @Nullable String detail, URI uri, Map<String, Object> properties) {
        Assert.notNull(status, "HttpStatusCode is required");
        ProblemDetail problemDetail = forStatus(status.value());
        problemDetail.setDetail(detail);
        problemDetail.setInstance(uri);
        problemDetail.setProperties(properties);
        return problemDetail;
    }
}
