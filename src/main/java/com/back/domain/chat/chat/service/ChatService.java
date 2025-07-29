package com.back.domain.chat.chat.service;

import com.back.domain.chat.chat.dto.ChatRoomDto;
import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.chat.chat.entity.Message;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import com.back.domain.chat.chat.repository.MessageRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PostRepository postRepository;

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

    public List<MessageDto> getChatRoomMessages(Long chatRoomId) {
        // 채팅방 존재 확인
        if( !chatRoomRepository.existsById(chatRoomId)) {
            throw new ServiceException("404-4", "존재하지 않는 채팅방입니다.");
        }

        // 메시지 조회 (시간순 정렬)
        List<Message> messages = messageRepository.findByChatRoomId(chatRoomId);

        messages.sort(Comparator.comparing(Message::getCreatedAt));
        // Entity -> DTO 변환
        return messages.stream()
                .map(message -> {
                    MessageDto dto = new MessageDto();
                    dto.setSenderId(message.getSender().getId());
                    dto.setSenderName(message.getSender().getName());
                    dto.setChatRoomId(message.getChatRoom().getId());
                    dto.setContent(message.getContent());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void createChatRoom(Long postId, String userName) {
        if(userName == null || userName.isEmpty()) {
            throw new ServiceException("400-1", "로그인 하셔야 합니다.");
        }


        if (chatRoomRepository.findByPostIdAndName(postId, userName).isPresent()) {
            throw new ServiceException("409-1", "이미 생성된 채팅방이 있습니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 게시글입니다."));

        ChatRoom chatRoom = new ChatRoom(post, userName);
        chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoomDto> getMyChatRooms(Principal principal) {
        if(principal == null || principal.getName() == null || principal.getName().isEmpty()) {
            throw new ServiceException("400-1", "로그인 하셔야 합니다.");
        }

        List<ChatRoom> chatRooms = chatRoomRepository.findByNameOrderByCreatedAtDesc(principal.getName());

        return chatRooms.stream()
                .map(chatRoom -> {
                    // 마지막 메시지 조회
                    Message lastMessage = messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId());
                    String lastContent = (lastMessage != null) ? lastMessage.getContent() : "메시지가 없습니다.";

                    return new ChatRoomDto(
                        chatRoom.getId(),
                        chatRoom.getName(),
                        chatRoom.getPost().getId(),
                        lastContent
                    );
                })
                .collect(Collectors.toList());
    }

    public ChatRoomDto deleteChatRoom(Long chatRoomId, String name) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ServiceException("404-4", "존재하지 않는 채팅방입니다."));


        return null;
    }
}
