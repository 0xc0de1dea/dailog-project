package com.example.dailog.common.auth.controller;

import com.example.dailog.common.auth.dto.TokenResponse;
import com.example.dailog.common.auth.service.AuthService;
import com.example.dailog.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    public record RefreshRequest(String refreshToken) {}

    // Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @RequestBody RefreshRequest request
    ) {
        TokenResponse response =
                authService.reissue(request.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody RefreshRequest request
    ) {
        authService.logout(request.refreshToken());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent());
    }
}