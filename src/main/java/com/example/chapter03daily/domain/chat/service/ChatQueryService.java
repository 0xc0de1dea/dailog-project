package com.example.chapter03daily.domain.chat.service;

import com.example.chapter03daily.domain.chat.dto.ChatMessageResponse;
import com.example.chapter03daily.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatQueryService {

    private final ChatMessageRepository repository;

    public List<ChatMessageResponse> getRecentMessages(int size) {
        Pageable pageable = PageRequest.of(0, size);

        return repository.findRecentMessages(pageable)
                .stream()
                .map(ChatMessageResponse::new)
                .toList();
    }

    public List<ChatMessageResponse> getMessageBefore(Long lastMessageId, int size) {
        Pageable pageable = PageRequest.of(0, size);

        return repository.findMessagesBefore(lastMessageId, pageable)
                .stream()
                .map(ChatMessageResponse::new)
                .toList();
    }
}
