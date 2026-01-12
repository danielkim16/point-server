package com.musinsa.data.point.model;

public class NotEnoughPointException extends RuntimeException {
    public NotEnoughPointException() {
        super("보유 포인트가 부족합니다.");
    }

    public NotEnoughPointException(String message) {
        super(message);
    }
}
