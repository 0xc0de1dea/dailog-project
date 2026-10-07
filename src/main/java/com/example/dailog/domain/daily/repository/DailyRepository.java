package com.example.dailog.domain.daily.repository;

import com.example.dailog.domain.daily.entity.Daily;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyRepository extends JpaRepository<Daily, Long>, DailyCustomRepository {

}
