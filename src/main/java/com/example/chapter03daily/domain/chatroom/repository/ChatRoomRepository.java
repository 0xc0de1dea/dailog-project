package com.example.chapter03daily.domain.chatroom.repository;

import com.example.chapter03daily.domain.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
