package com.example.dailog.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DailyImproveRequest(
        @Size(max = 50)
        String title,

        @NotBlank
        @Size(max = 1000)
        String content,

        @NotBlank
        String mode
) {}