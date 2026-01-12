package com.musinsa.data.point.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PointConfigType {
    MAX_EARN_PER_ONCE("1회 최대 적립 한도"),
    MAX_BALANCE_LIMIT("개인별 최대 보유 한도");

    private final String description;
}
