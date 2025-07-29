package com.back.domain.chat.chat.service;

import com.back.domain.chat.chat.dto.ChatRoomDto;
import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.chat.chat.entity.Message;
import com.back.domain.chat.chat.entity.RoomParticipant;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import com.back.domain.chat.chat.repository.MessageRepository;
import com.back.domain.chat.chat.repository.RoomParticipantRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RoomParticipantRepository roomParticipantRepository;

    @Transactional
    public Message saveMessage(MessageDto chatMessage) {
        Member sender = memberRepository.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new ServiceException("404-3", "존재하지 않는 사용자입니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(chatMessage.getChatRoomId())
                .orElseThrow(() -> new ServiceException("404-4", "존재하지 않는 채팅방입니다."));

        Message message = new Message(chatMessage , sender);
        message.setChatRoom(chatRoom);

        return messageRepository.save(message);
    }
    @Transactional
    public boolean isParticipant(Long chatRoomId, Long memberId) {
        return roomParticipantRepository.existsByChatRoomIdAndMemberIdAndIsActiveTrue(chatRoomId, memberId);
    }
    @Transactional
    public List<MessageDto> getChatRoomMessages(Long chatRoomId, Principal principal) {
        Member member = memberRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ServiceException("404-3", "존재하지 않는 사용자입니다."));
        Long requesterId = member.getId();

        // 채팅방 존재 확인
        if( !chatRoomRepository.existsById(chatRoomId)) {
            throw new ServiceException("404-4", "존재하지 않는 채팅방입니다.");
        }

        // 권한 체크 추가
        if(!isParticipant(chatRoomId, requesterId)) {
            throw new ServiceException("403-1", "채팅방 참여자만 메시지를 조회할 수 있습니다.");
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
                    dto.setSenderEmail(message.getSender().getEmail());
                    dto.setChatRoomId(message.getChatRoom().getId());
                    dto.setContent(message.getContent());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public Long createChatRoom(Long postId, String userEmail) {
        if(userEmail == null || userEmail.isEmpty()) {
            throw new ServiceException("400-1", "로그인 하셔야 합니다.");
        }

        // 이메일로 Member 엔티티 조회 (JWT의 principal.getName()은 이메일을 반환)
        Member requester = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ServiceException("404-3", "존재하지 않는 사용자입니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 게시글입니다."));

        Member postAuthor = post.getMember();

        // 본인 게시글에는 채팅방을 만들 수 없도록 제한
        if (requester.getId().equals(postAuthor.getId())) {
            throw new ServiceException("400-2", "본인의 게시글에는 채팅할 수 없습니다.");
        }

        // requester가 참여한 해당 게시글의 채팅방들 찾기
        List<RoomParticipant> requesterParticipations = roomParticipantRepository
            .findByChatRoomPostIdAndMemberIdAndIsActiveTrue(postId, requester.getId());
        
        // 각 채팅방에서 postAuthor도 참여하고 있고, 참여자가 2명인지 확인
        for (RoomParticipant participation : requesterParticipations) {
            ChatRoom chatRoom = participation.getChatRoom();
            
            // 이 채팅방의 참여자 수와 postAuthor 참여 여부 확인
            List<RoomParticipant> participants = roomParticipantRepository
                .findByChatRoomIdAndIsActiveTrue(chatRoom.getId());
                
            if (participants.size() == 2) {
                boolean hasPostAuthor = participants.stream()
                    .anyMatch(p -> p.getMember().getId().equals(postAuthor.getId()));
                    
                if (hasPostAuthor) {
                    // 1대1 채팅방 발견!
                    return chatRoom.getId();
                }
            }
        }

        // 기존 1대1 채팅방이 없다면 새로 생성
        ChatRoom chatRoom = new ChatRoom(post, requester);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 정확히 2명만 참여자로 추가
        roomParticipantRepository.save(new RoomParticipant(savedChatRoom, requester));
        roomParticipantRepository.save(new RoomParticipant(savedChatRoom, postAuthor));

        return savedChatRoom.getId();
    }
    @Transactional
    public List<ChatRoomDto> getMyChatRooms(Principal principal) {
        if(principal == null || principal.getName() == null || principal.getName().isEmpty()) {
            throw new ServiceException("400-1", "로그인 하셔야 합니다.");
        }

        // 이메일로 Member 엔티티 조회 (JWT의 principal.getName()은 이메일을 반환)
        Member member = memberRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ServiceException("404-3", "존재하지 않는 사용자입니다."));

        // Member ID로 채팅방 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());

        return chatRooms.stream()
                .map(chatRoom -> {
                    // 마지막 메시지 조회
                    Message lastMessage = messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId());
                    String lastContent = (lastMessage != null) ? lastMessage.getContent() : "메시지가 없습니다.";

                    return new ChatRoomDto(
                        chatRoom.getId(),
                        chatRoom.getRoomName(),
                        chatRoom.getPost().getId(),
                        lastContent
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ServiceException("404-4", "존재하지 않는 채팅방입니다."));

        chatRoomRepository.delete(chatRoom);
    }
    @Transactional
    public Member findByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException("404-3", "존재하지 않는 사용자입니다."));
        return member;
    }

    @Transactional
    public List<String> getParticipants(Long chatRoomId) {
        return roomParticipantRepository.findByChatRoomIdAndIsActiveTrue(chatRoomId)
                .stream()
                .map(participant -> participant.getMember().getEmail())
                .collect(Collectors.toList());
    }
}
