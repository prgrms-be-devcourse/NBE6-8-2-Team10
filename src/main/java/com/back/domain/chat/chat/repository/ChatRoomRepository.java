package com.back.domain.chat.chat.repository;

import com.back.domain.chat.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    ChatRoom findByName(String name);
    List<ChatRoom> findByPostId(Long postId); // 한개의 상품에 여러개의 채팅방이 있을 수 있음

    Optional<ChatRoom> findByPostIdAndUserName(Long postId, String userName);

    // 사용자명으로 채팅방 목록 조회 (Principal용)
    List<ChatRoom> findByNameOrderByCreatedAtDesc(String userName);
}
