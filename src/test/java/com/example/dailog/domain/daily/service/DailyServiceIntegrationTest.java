package com.example.dailog.domain.daily.service;

import com.example.dailog.domain.daily.dto.DailyDto;
import com.example.dailog.domain.daily.entity.Daily;
import com.example.dailog.domain.daily.repository.DailyRepository;
import com.example.dailog.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DailyServiceIntegrationTest {

    @Autowired
    private DailyService dailyService;

    @Autowired
    private DailyRepository dailyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    @Test
    @DisplayName("일정 생성 통합 테스트 - 실제 DB에 저장 및 조회 검증")
    void createDaily_통합테스트_success() {

        // given
        String title = "testTitle";
        String content = "testContent";
        String email = "test@email.com";
        String password = "12345678";

        User user = new User(
                email,
                password,
                Collections.emptyList()
        );

        DailyDto.Request request = new DailyDto.Request(
                title,
                content,
                password
        );

        // when
        DailyDto.Response response = dailyService.create(user, request);

        // then
        Optional<Daily> daily = dailyRepository.findById(1L);

        assertThat(daily.get().getAuthor()).isEqualTo(email);

    }

}