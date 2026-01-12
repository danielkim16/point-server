package com.musinsa.data.point.infrastructure.persistence.repository;

import com.musinsa.data.point.model.PointEarning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointEarningJpaRepository extends JpaRepository<PointEarning, Long> {
}
