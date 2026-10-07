package com.example.dailog.domain.daily.repository;

import com.example.dailog.domain.daily.entity.DailyLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyLikeRepository extends JpaRepository<DailyLike, Long> {

    boolean existsByDailyIdAndUserId(Long dailyId, Long userId);

    long countByDailyId(Long dailyId);
}
