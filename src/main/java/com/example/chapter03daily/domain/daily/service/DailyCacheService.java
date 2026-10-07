package com.example.chapter03daily.domain.daily.service;

import com.example.chapter03daily.domain.daily.dto.DailyDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DailyCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_DAILY_PREFIX = "daily:";

    public DailyDetailResponse getDailyCache(Long dailyId) {
        Object cached = redisTemplate.opsForValue().get("daily:" + dailyId);

        if (cached == null) {
            return null;
        }

        return objectMapper.convertValue(cached, DailyDetailResponse.class);
    }

    public void saveDailyCache(long id, DailyDetailResponse dailyDto) {
        String key = CACHE_DAILY_PREFIX + id;

        try {
            redisTemplate.opsForValue().set(
                    key,
                    dailyDto,
                    10,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    public void deleteDailyCache(long id) {
        String key = CACHE_DAILY_PREFIX + id;
        redisTemplate.delete(key);
    }

}
