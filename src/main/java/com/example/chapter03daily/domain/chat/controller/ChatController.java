package com.example.chapter03daily.domain.chat.controller;

import com.example.chapter03daily.common.exception.ErrorCode;
import com.example.chapter03daily.common.exception.ServiceException;
import com.example.chapter03daily.domain.chat.dto.ChatMessageDto;
import com.example.chapter03daily.domain.chat.entity.ChatMessage;
import com.example.chapter03daily.domain.chat.repository.ChatMessageRepository;
import com.example.chapter03daily.domain.chatroom.entity.ChatRoom;
import com.example.chapter03daily.domain.chatroom.repository.ChatRoomRepository;
import com.example.chapter03daily.domain.user.entity.User;
import com.example.chapter03daily.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(
            ChatMessageDto chatMessageDto
    ) {
        User sender = userRepository
                .findById(chatMessageDto.getSenderId())
                        .orElseThrow(
                                () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                        );

        ChatRoom chatRoom = chatRoomRepository
                .findById(chatMessageDto.getRoomId())
                        .orElseThrow(
                                () -> new ServiceException(ErrorCode.CHATROOM_NOT_FOUND)
                        );

        ChatMessage message = new ChatMessage(chatRoom, sender, chatMessageDto.getContent());
        chatMessageRepository.save(message);

        messagingTemplate.convertAndSend("/sub/chat/" + chatMessageDto.getRoomId(), chatMessageDto);
    }
}
