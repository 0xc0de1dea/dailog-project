package com.example.dailog.common.auth.service;

import com.example.dailog.common.auth.dto.TokenResponse;
import com.example.dailog.common.enums.UserRoleEnum;
import com.example.dailog.common.exception.ErrorCode;
import com.example.dailog.common.exception.ServiceException;
import com.example.dailog.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // Refresh Token으로 토큰 재발급
    public TokenResponse reissue(String refreshToken) {

        // 1. Refresh Token 검증
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new ServiceException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        // 2. 이메일 추출
        String email = jwtUtil.extractEmail(refreshToken);

        // 3. Redis 저장 토큰과 일치하는지 확인
        if (!refreshTokenService.matches(email, refreshToken)) {
            throw new ServiceException(
                    ErrorCode.UNMATCHED_REFRESH_TOKEN
            );
        }

        // 4. 사용자 권한 추출
        UserRoleEnum role;

        try {
            role = UserRoleEnum.valueOf(
                    jwtUtil.extractRole(refreshToken)
            );
        } catch (IllegalArgumentException e) {
            throw new ServiceException(
                    ErrorCode.INVALID_USER_ROLE
            );
        }

        // 5. 새 토큰 발급
        String newAccessToken =
                jwtUtil.generateAccessToken(email, role);

        String newRefreshToken =
                jwtUtil.generateRefreshToken(email, role);

        // 6. Refresh Token 교체
        refreshTokenService.save(email, newRefreshToken);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    // 로그아웃
    public void logout(String refreshToken) {

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new ServiceException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        String email = jwtUtil.extractEmail(refreshToken);

        // 현재 로그인 세션의 토큰이 맞는 경우에만 삭제
        if (!refreshTokenService.matches(email, refreshToken)) {
            throw new ServiceException(
                    ErrorCode.UNMATCHED_REFRESH_TOKEN
            );
        }

        refreshTokenService.delete(email);
    }
}