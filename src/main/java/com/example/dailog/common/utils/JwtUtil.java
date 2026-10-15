package com.example.dailog.common.utils;

import com.example.dailog.common.enums.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    public static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L; // 60분
    private static final long REFRESH_TOKEN_TIME = 14L * 24 * 60 * 60 * 1000; // 14일

    private static final String TOKEN_TYPE = "tokenType";
    private static final String ACCESS = "ACCESS";
    private static final String REFRESH = "REFRESH";

    @Value("${jwt.secret.key}")           // application.yml 에 있는 key 가져옴
    private String secretKeyString;

    private SecretKey key;
    private JwtParser parser;

    /**
     * 빈 초기화 메서드
     * @PostConstruct 어플리케이션 실행 될 때 가장 먼저 실행 되게 하는 어노테이션
     */

    @PostConstruct
    public void init() {
        byte[] bytes = Decoders.BASE64.decode(secretKeyString);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.parser = Jwts.parser()
                .verifyWith(this.key)
                .build();
    }


    // Access Token 생성
    public String generateAccessToken(
            String email,
            UserRoleEnum role
    ) {
        return generateToken(email, role, ACCESS, ACCESS_TOKEN_TIME);
    }

    // Refresh Token 생성
    public String generateRefreshToken(
            String email,
            UserRoleEnum role
    ) {
        return generateToken(email, role, REFRESH, REFRESH_TOKEN_TIME);
    }

    private String generateToken(
            String email,
            UserRoleEnum role,
            String tokenType,
            long expirationTime
    ) {
        Date now = new Date();

        return BEARER_PREFIX + Jwts.builder()
                .subject(email)
                .claim("email", email)
                .claim("role", role.name())
                .claim(TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // 서명 및 만료 검증
    private Claims parseClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    // Access Token 검증
    public boolean validateAccessToken(String token) {
        return validateTokenType(token, ACCESS);
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, REFRESH);
    }

    private boolean validateTokenType(
            String token,
            String expectedType
    ) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            Claims claims = parseClaims(token);

            return expectedType.equals(
                    claims.get(TOKEN_TYPE, String.class)
            );
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.toString());
            return false;
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long getRefreshTokenExpirationMillis() {
        return REFRESH_TOKEN_TIME;
    }
}
