package com.example.chapter03daily.domain.chat.controller;

import com.example.chapter03daily.common.dto.ApiResponse;
import com.example.chapter03daily.domain.chat.dto.ChatMessageResponse;
import com.example.chapter03daily.domain.chat.service.ChatQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatQueryController {

    private final ChatQueryService chatQueryService;

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(chatQueryService.getRecentMessages(roomId, size)));
    }

    @GetMapping("/rooms/{roomId}/messages/before/{lastMessageId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessagesBefore(
            @PathVariable Long roomId,
            @PathVariable Long lastMessageId,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(chatQueryService.getMessageBefore(roomId, lastMessageId, size)));
    }
}
