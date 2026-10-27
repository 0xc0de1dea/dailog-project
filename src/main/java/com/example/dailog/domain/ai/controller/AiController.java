package com.example.dailog.domain.ai.controller;

import com.example.dailog.common.dto.ApiResponse;
import com.example.dailog.domain.ai.dto.DailyImproveRequest;
import com.example.dailog.domain.ai.dto.DailyImproveResponse;
import com.example.dailog.domain.ai.service.AiWritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiWritingService aiWritingService;

    @PostMapping("/daily/improve")
    public ResponseEntity<ApiResponse<DailyImproveResponse>> improve(
            @Valid @RequestBody DailyImproveRequest request
    ) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(aiWritingService.improve(request)));
    }
}