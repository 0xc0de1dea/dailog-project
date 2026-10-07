package com.example.dailog.domain.daily.service;

import com.example.dailog.common.exception.ErrorCode;
import com.example.dailog.common.exception.ServiceException;
import com.example.dailog.domain.daily.dto.DailyDetailResponse;
import com.example.dailog.domain.daily.dto.DailyDto;
import com.example.dailog.domain.daily.entity.Daily;
import com.example.dailog.domain.daily.repository.DailyRepository;
import com.example.dailog.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyServiceTest {

    @Mock
    private DailyRepository dailyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DailyService dailyService;


    @Test
    @DisplayName("일정 생성 성공 - 유효한 사용자와 내용이 주어졌을 때 - 성공케이스")
    void createDaily_success() {

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

        Daily daily = new Daily(
                title,
                content,
                email,
                passwordEncoder.encode(password)
        );

        ReflectionTestUtils.setField(daily, "id", 1L);

        when(dailyRepository.saveAndFlush(any(Daily.class))).thenReturn(daily);

        // when
        DailyDto.Response response = dailyService.create(user, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(title);
        assertThat(response.getContent()).isEqualTo(content);
        assertThat(daily.getId()).isEqualTo(1L);
        assertThat(response.getAuthor()).isEqualTo(email);

        verify(dailyRepository, times(1)).saveAndFlush(any(Daily.class));

    }

    @Test
    @DisplayName("아이디로 일정 조회 - 성공")
    void getDailyDetailById_success() {

        // given
        String title = "testTitle";
        String content = "testContent";
        String email = "test@email.com";
        String password = "12345678";

        Daily daily = new Daily(
                title,
                content,
                email,
                passwordEncoder.encode(password)
        );

        ReflectionTestUtils.setField(daily, "id", 1L);

        when(dailyRepository.findById(anyLong())).thenReturn(Optional.of(daily));

        // when
        DailyDetailResponse response = dailyService.findOne(1L);

        // then
        assertThat(response.getAuthor()).isEqualTo(email);

    }

    @Test
    @DisplayName("아이디로 일정 조회 - 실패 - 사용자가 없는 경우")
    void getDailyDetailById_fail_case01() {

        // given
        when(dailyRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when && then
        assertThatThrownBy(() -> dailyService.findOne(anyLong()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining(ErrorCode.DAILY_NOT_FOUND.getMessage());

    }

}