package com.example.dailog.domain.chat.repository;

import com.example.dailog.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
        select m
        from ChatMessage m
        join fetch m.sender
        where m.chatRoom.id = :roomId
        order by m.id desc
    """)
    List<ChatMessage> findRecentMessages(
            @Param("roomId") Long roomId,
            Pageable pageable
    );

    @Query("""
        select m
        from ChatMessage m
        join fetch m.sender
        where m.id < :lastMessageId and m.chatRoom.id = :roomId
        order by m.id desc
    """)
    List<ChatMessage> findMessagesBefore(
            @Param("roomId") Long roomId,
            @Param("lastMessageId") Long lastMessageId,
            Pageable pageable
    );
}
