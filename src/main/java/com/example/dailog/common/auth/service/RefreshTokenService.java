package com.example.dailog.common.auth.service;

import com.example.dailog.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.example.dailog.common.constant.MagicNumber.SEVEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh-token:";

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    // Refresh Token 저장
    public void save(String email, String refreshToken) {
        refreshToken = refreshToken.substring(SEVEN);

        redisTemplate.opsForValue().set(
                KEY_PREFIX + email,
                refreshToken,
                jwtUtil.getRefreshTokenExpirationMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    // 저장된 Refresh Token 조회
    public String findByEmail(String email) {
        return redisTemplate.opsForValue()
                .get(KEY_PREFIX + email);
    }

    // 저장된 토큰과 요청 토큰 일치 여부 확인
    public boolean matches(String email, String refreshToken) {
        String savedToken = findByEmail(email);

        log.info("::: " + savedToken + " " + refreshToken + " :::");

        return savedToken != null
                && savedToken.equals(refreshToken);
    }

    // Refresh Token 삭제
    public void delete(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}