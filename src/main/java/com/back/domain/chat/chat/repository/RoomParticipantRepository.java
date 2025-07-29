package com.back.domain.chat.chat.repository;

import com.back.domain.chat.chat.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    boolean existsByChatRoomIdAndMemberIdAndIsActiveTrue(Long chatRoomId, Long memberId);
    List<RoomParticipant> findByChatRoomIdAndIsActiveTrue(Long chatRoomId);
}
