package com.example.chapter03daily.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessageDto {

    private Long roomId;
    private Long senderId;
    private String content;

}