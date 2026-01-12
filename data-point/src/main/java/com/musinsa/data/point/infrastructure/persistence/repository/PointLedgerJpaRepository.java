package com.musinsa.data.point.infrastructure.persistence.repository;

import com.musinsa.data.point.model.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLedgerJpaRepository extends JpaRepository<PointLedger, Long> {
}
