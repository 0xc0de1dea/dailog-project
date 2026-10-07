package com.example.chapter03daily.domain.user.service;

import com.example.chapter03daily.common.exception.ErrorCode;
import com.example.chapter03daily.common.exception.ServiceException;
import com.example.chapter03daily.common.utils.JwtUtil;
import com.example.chapter03daily.domain.user.dto.UserDto;
import com.example.chapter03daily.domain.user.dto.request.LoginRequest;
import com.example.chapter03daily.domain.user.entity.User;
import com.example.chapter03daily.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserDto.Response register(UserDto.Request request) {
        User saved = userRepository.saveAndFlush(
                new User(
                        request.getName(),
                        request.getEmail(),
                        passwordEncoder.encode(request.getPassword()),
                        request.getRole()
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
    public String login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ServiceException(ErrorCode.INVALID_PASSWORD);
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRoleEnum());
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
