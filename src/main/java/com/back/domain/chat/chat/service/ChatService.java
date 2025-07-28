package com.back.domain.chat.chat.service;

import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.chat.chat.entity.Message;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import com.back.domain.chat.chat.repository.MessageRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    public Message saveMessage(MessageDto chatMessage) {
        Member sender = memberRepository.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new ServiceException("404-3", "존재하지 않는 사용자입니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(chatMessage.getChatRoomId())
                .orElseThrow(() -> new ServiceException("404-4", "존재하지 않는 채팅방입니다."));

        Message message = new Message(chatMessage , sender);
        message.setChatRoom(chatRoom);

        return messageRepository.save(message);
    }

    public boolean isParticipant(Long chatRoomId, Long senderId) {
        return false;
    }
}
