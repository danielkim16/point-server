package com.musinsa.data.point.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "POINT_CONFIG")
public class PointConfig {

    @Id
    @Comment("설정 키 (예: MAX_EARN_PER_ONCE)")
    @Column(length = 50)
    private String configKey;

    @Comment("설정 값")
    @Column(nullable = false)
    private String configValue;

    @Comment("설정 설명")
    private String description;

    public long getValueAsLong() {
        try {
            return Long.parseLong(this.configValue);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("설정값(" + configKey + ")이 숫자가 아닙니다: " + configValue);
        }
    }
}