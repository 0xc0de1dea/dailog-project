package com.example.chapter03daily.domain.chat.repository;

import com.example.chapter03daily.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

}
