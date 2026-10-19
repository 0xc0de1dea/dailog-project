package com.example.dailog.domain.ai.dto;

public record DailyImproveResponse(
        String title,
        String content,
        String mode
) {}