package com.example.chapter03daily.common.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        RedisChatMessage redisChatMessage = objectMapper.readValue(
                message.getBody(),
                RedisChatMessage.class
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/" + redisChatMessage.getRoomId(),
                redisChatMessage
        );

    }
}
