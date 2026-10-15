package com.example.dailog.domain.user.service;

import com.example.dailog.common.auth.dto.TokenResponse;
import com.example.dailog.common.auth.service.RefreshTokenService;
import com.example.dailog.common.enums.UserRoleEnum;
import com.example.dailog.common.exception.ErrorCode;
import com.example.dailog.common.exception.ServiceException;
import com.example.dailog.common.utils.JwtUtil;
import com.example.dailog.domain.user.dto.UserDto;
import com.example.dailog.domain.user.dto.request.LoginRequest;
import com.example.dailog.domain.user.entity.User;
import com.example.dailog.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserDto.Response register(UserDto.Request request) {
        User saved = userRepository.saveAndFlush(
                new User(
                        request.getName(),
                        request.getEmail(),
                        passwordEncoder.encode(request.getPassword()),
                        UserRoleEnum.NORMAL
                )
        );

        return UserDto.Response.build(
                saved.getName(),
                saved.getEmail(),
                saved.getRoleEnum(),
                saved.getCreatedAt(),
                null
        );
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (!passwordEncoder.matches(
                password,
                user.getPassword()
        )) {
            throw new ServiceException(
                    ErrorCode.INVALID_PASSWORD
            );
        }

        // 1. Access Token 발급
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRoleEnum()
        );

        // 2. Refresh Token 발급
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getEmail(),
                user.getRoleEnum()
        );

        // 3. Redis에 Refresh Token 저장
        refreshTokenService.save(
                user.getEmail(),
                refreshToken
        );

        // 4. 토큰 응답 반환
        return new TokenResponse(
                accessToken,
                refreshToken
        );
    }

    @Transactional(readOnly = true)
    public UserDto.Response findOne(long id) {
        User saved = userRepository.findById(id)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        return UserDto.Response.build(
                saved.getName(),
                saved.getEmail(),
                saved.getRoleEnum(),
                saved.getCreatedAt(),
                saved.getModifiedAt()
        );
    }

    @Transactional
    public UserDto.Response update(org.springframework.security.core.userdetails.User user, UserDto.Request request) {
        String email = user.getUsername();

        User saved = userRepository.findUserByEmail(email)
                        .orElseThrow(
                                () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                        );

        saved.update(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        return UserDto.Response.build(
                saved.getName(),
                saved.getEmail(),
                saved.getRoleEnum(),
                null,
                saved.getModifiedAt()
        );
    }

    @Transactional
    public void delete(org.springframework.security.core.userdetails.User user) {
        String email = user.getUsername();

        User saved = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        userRepository.deleteById(saved.getId());
    }
}
