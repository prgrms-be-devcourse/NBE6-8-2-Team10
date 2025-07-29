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

        // 먼저 해당 게시글에 대한 채팅방이 이미 존재하는지 확인
        List<ChatRoom> existingChatRooms = chatRoomRepository.findByPostId(postId);
        
        if (!existingChatRooms.isEmpty()) {
            // 첫 번째 채팅방을 사용 (이론적으로 하나만 있어야 함)
            ChatRoom chatRoom = existingChatRooms.get(0);
            
            // 요청자가 이미 이 채팅방의 참여자인지 확인
            boolean isAlreadyParticipant = roomParticipantRepository
                    .existsByChatRoomIdAndMemberIdAndIsActiveTrue(chatRoom.getId(), requester.getId());
            
            if (!isAlreadyParticipant) {
                // 참여자가 아니라면 참여자로 추가
                roomParticipantRepository.save(new RoomParticipant(chatRoom, requester));
            }
            
            // 기존 채팅방 ID 반환
            return chatRoom.getId();
        }

        // 해당 게시글에 대한 채팅방이 없다면 새로 생성
        ChatRoom chatRoom = new ChatRoom(post, requester);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 채팅방 생성자와 게시글 작성자를 참여자로 추가
        roomParticipantRepository.save(new RoomParticipant(savedChatRoom, requester));
        
        // 게시글 작성자와 채팅방 생성자가 다른 경우에만 게시글 작성자도 참여자로 추가
        if (!requester.getId().equals(postAuthor.getId())) {
            roomParticipantRepository.save(new RoomParticipant(savedChatRoom, postAuthor));
        }
        
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
