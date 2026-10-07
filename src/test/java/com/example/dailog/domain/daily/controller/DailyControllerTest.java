package com.example.dailog.domain.daily.controller;

import com.example.dailog.common.enums.UserRoleEnum;
import com.example.dailog.common.utils.JwtUtil;
import com.example.dailog.domain.user.dto.UserDto;
import com.example.dailog.domain.user.entity.User;
import com.example.dailog.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class DailyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() {
        String name = "이재환";
        String password = "12345678";
        String email = "test2@email.com";
        UserRoleEnum role = UserRoleEnum.ADMIN;

        UserDto.Request request = new UserDto.Request(
            name, email, password, role
        );

        User user = userRepository.saveAndFlush(
                new User(
                        request.getName(),
                        request.getEmail(),
                        passwordEncoder.encode(request.getPassword()),
                        request.getRole()
                )
        );

        token = jwtUtil.generateToken(user.getEmail(), user.getRoleEnum());
    }

    @Test
    @DisplayName("POST /api/dailies - 게시글 생성 요청 성공")
    void createDaily_통합테스트_success() throws Exception {

        String requestBody =
                """
                        {
                            "title": "제목",
                            "content": "내용",
                            "password": "1234"
                        }
                """;

        mockMvc.perform(post("/api/dailies")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.content").value("내용"));
    }

}