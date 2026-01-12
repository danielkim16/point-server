package com.musinsa.data.point.infrastructure.persistence.repository;

import com.musinsa.data.point.model.PointConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointConfigJpaRepository extends JpaRepository<PointConfig, Long> {
}
