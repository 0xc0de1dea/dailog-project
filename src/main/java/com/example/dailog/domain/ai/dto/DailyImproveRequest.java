package com.example.dailog.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DailyImproveRequest(
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 5000)
        String content,

        @NotBlank
        String mode
) {}