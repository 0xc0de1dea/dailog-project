package com.example.dailog.domain.chat.controller;

import com.example.dailog.common.dto.ApiResponse;
import com.example.dailog.domain.chatroom.entity.ChatRoom;
import com.example.dailog.domain.chatroom.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;

    @GetMapping
    public List<ChatRoom> getAll() {
        return chatRoomRepository.findAll();
    }

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
