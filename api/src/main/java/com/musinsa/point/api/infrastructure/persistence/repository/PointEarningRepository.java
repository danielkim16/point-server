package com.musinsa.point.api.infrastructure.persistence.repository;

import com.musinsa.data.point.infrastructure.persistence.repository.PointEarningJpaRepository;
import com.musinsa.data.point.model.PointEarning;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointEarningRepository extends PointEarningJpaRepository {

    /**
     * 가용 포인트를 조회한다.
     */
    @Query("SELECT p FROM PointEarning p " +
            "WHERE p.memberId = :memberId " +
            "AND p.expireAt > :now " +
            "AND p.balanceAmount > 0 " +
            "ORDER BY p.isManual DESC, p.expireAt ASC")
    List<PointEarning> findAvailablePoints(@Param("memberId") Long memberId,
                                           @Param("now") LocalDateTime now);

    /**
     * 특정 회원의 유효한 포인트 총 잔액을 조회한다.
     */
    @Query("SELECT COALESCE(SUM(p.balanceAmount), 0) " +
            "FROM PointEarning p " +
            "WHERE p.memberId = :memberId " +
            "AND p.expireAt > :now")
    Long sumValidBalance(@Param("memberId") Long memberId,
                         @Param("now") LocalDateTime now);

    /**
     * 포인트키 중복 여부를 확인한다.
     */
    boolean existsByPointKey(String pointKey);

    /**
     * 포인트 키로 조회한다.
     */
    Optional<PointEarning> findByPointKey(String pointKey);
}
