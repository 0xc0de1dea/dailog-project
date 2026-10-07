package com.example.dailog.common.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRedisSubscriber
        implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    private final JacksonJsonRedisSerializer<RedisChatMessage>
            chatRedisSerializer;

    @Override
    public void onMessage(
            Message message,
            byte[] pattern
    ) {

        try {

            RedisChatMessage redisChatMessage =
                    chatRedisSerializer.deserialize(
                            message.getBody()
                    );

            if (redisChatMessage == null) {
                return;
            }

            System.out.println(
                    "[REDIS] 메시지 수신 - roomId="
                            + redisChatMessage.getRoomId()
                            + ", sender="
                            + redisChatMessage.getSenderName()
                            + ", content="
                            + redisChatMessage.getContent()
            );

            /*
             * Redis Pub/Sub
             * ↓
             * 현재 Spring 서버의 WebSocket
             * ↓
             * 해당 채팅방 사용자들에게 전달
             */
            messagingTemplate.convertAndSend(
                    "/sub/chat/"
                            + redisChatMessage.getRoomId(),
                    redisChatMessage
            );

        } catch (Exception e) {

            System.err.println(
                    "[REDIS] 채팅 메시지 처리 실패"
            );

            e.printStackTrace();
        }
    }
}