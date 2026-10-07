package com.example.dailog.common.utils;

import com.example.dailog.common.enums.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.example.dailog.common.utils.JwtUtil.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET_KEY = "mysecretkeyfortestcodepracticemysecretkeyfortestcodepractice";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(jwtUtil, "secretKeyString", SECRET_KEY);

        jwtUtil.init();
    }

    @Test
    @DisplayName("JWT 토큰 생성 시 username과 role 정보가 정상적으로 포함이 되었는지 테스트")
    void generateToken_정상케이스() {

        // given
        String email = "user1@email.com";
        UserRoleEnum role = UserRoleEnum.ADMIN;

        // when
        String jwtToken = jwtUtil.generateToken(email, role);
        String jwt = jwtToken.substring(BEARER_PREFIX.length());

        // then
        assertThat(jwtToken).startsWith(BEARER_PREFIX);

        JwtParser parser = (JwtParser) ReflectionTestUtils.getField(jwtUtil, "parser");

        assert parser != null;

        Claims claims = parser.parseSignedClaims(jwt).getPayload();

        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.get("role", String.class)).isEqualTo(role.name());

    }

    @Test
    @DisplayName("유효한 토큰인지 아닌지 검사 유효하면 true 반환 - 성공케이스")
    void validateToken_성공케이스() {

        // given
        String email = "user1@email.com";
        UserRoleEnum role = UserRoleEnum.ADMIN;

        String token = jwtUtil.generateToken(email, role)
                .substring(BEARER_PREFIX.length());

        // when
        boolean result = jwtUtil.validateToken(token);

        // then
        assertThat(result).isTrue();

    }

    @Test
    @DisplayName("유효한 토큰인지 아닌지 검사 유효하면 true 반환 - 실패케이스 - 잘못된 jwt 토큰 제공")
    void validateToken_실패케이스_01() {

        // given
        String token = "thisiswrongtoken";

        // when
        boolean result = jwtUtil.validateToken(token);

        // then
        assertThat(result).isFalse();

    }
}