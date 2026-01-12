package com.musinsa.point.api.infrastructure.persistence.repository;

import com.musinsa.data.point.infrastructure.persistence.repository.PointConfigJpaRepository;
import com.musinsa.data.point.model.PointConfig;

import java.util.Optional;

public interface PointConfigRepository extends PointConfigJpaRepository {
    Optional<PointConfig> findByConfigKey(String configKey);
}
