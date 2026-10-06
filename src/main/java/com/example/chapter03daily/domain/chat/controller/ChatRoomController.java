package com.example.chapter03daily.domain.chat.controller;

import com.example.chapter03daily.common.dto.ApiResponse;
import com.example.chapter03daily.domain.chatroom.entity.ChatRoom;
import com.example.chapter03daily.domain.chatroom.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoom>> create(
            @RequestParam String name
    ) {
        ChatRoom chatRoom = new ChatRoom(name);

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(saved));
    }
}
