package com.example.chapter03daily.common.config.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RedisChatMessage {

    private Long roomId;

    private Long senderId;

    private String senderName;

    private String content;
}