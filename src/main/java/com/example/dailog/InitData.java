package com.example.dailog;

import com.example.dailog.common.enums.UserRoleEnum;
import com.example.dailog.domain.user.dto.UserDto;
import com.example.dailog.domain.user.entity.User;
import com.example.dailog.domain.user.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitData {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @PostConstruct
    @Transactional
    public void init() {
        List<User> userList =
                List.of(
                        new User("이재환", "user1@email.com", "12345678", UserRoleEnum.ADMIN),
                        new User("재환이", "user2@email.com", "12345678", UserRoleEnum.NORMAL)
                );

        for (User user : userList) {
            UserDto.Request request = new UserDto.Request(
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRoleEnum()
            );

            userService.register(request);
        }
    }
}
