package com.example.dailog.domain.chat.controller;

import com.example.dailog.common.config.redis.ChatRedisPublisher;
import com.example.dailog.common.config.redis.RedisChatMessage;
import com.example.dailog.common.exception.ErrorCode;
import com.example.dailog.common.exception.ServiceException;
import com.example.dailog.common.interceptor.AuthenticatedUser;
import com.example.dailog.domain.chat.dto.ChatMessageDto;
import com.example.dailog.domain.chat.dto.TypingIndicatorDto;
import com.example.dailog.domain.chat.entity.ChatMessage;
import com.example.dailog.domain.chat.repository.ChatMessageRepository;
import com.example.dailog.domain.chatroom.entity.ChatRoom;
import com.example.dailog.domain.chatroom.repository.ChatRoomRepository;
import com.example.dailog.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatRedisPublisher chatRedisPublisher;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(
            ChatMessageDto chatMessageDto,
            Principal principal
    ) {
//        User sender = userRepository
//                .findById(chatMessageDto.getSenderId())
//                        .orElseThrow(
//                                () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
//                        );

        User sender = AuthenticatedUser.fromPrincipal(principal);

        ChatRoom chatRoom = chatRoomRepository
                .findById(chatMessageDto.getRoomId())
                        .orElseThrow(
                                () -> new ServiceException(ErrorCode.CHATROOM_NOT_FOUND)
                        );

        ChatMessage message = new ChatMessage(chatRoom, sender, chatMessageDto.getContent());
        chatMessageRepository.save(message);

//        messagingTemplate.convertAndSend("/sub/chat/" + chatMessageDto.getRoomId(), chatMessageDto);

        RedisChatMessage redisChatMessage = new RedisChatMessage(
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent()
        );

        chatRedisPublisher.publish(chatRoom.getId(), redisChatMessage);
    }

    @MessageMapping("/chat.typing")
    public void typing(TypingIndicatorDto dto, Principal principal) {
        User user = AuthenticatedUser.fromPrincipal(principal);

        dto.setUserId(user.getId());
        dto.setUserName(user.getName());

        messagingTemplate.convertAndSend("/sub/chat/" + dto.getRoomId() + "/typing", dto);
    }
}
