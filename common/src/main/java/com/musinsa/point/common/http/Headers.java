package com.musinsa.point.common.http;

public class Headers {
    public static final String TRANSACTION_ID = "X-TransactionId";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer";

    private Headers(){
        throw new IllegalStateException();
    }
}
