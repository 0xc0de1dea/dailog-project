package com.example.dailog.common.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

    String accessToken;

    String refreshToken;
}