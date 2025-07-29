package com.back.domain.chat.chat.service;

import com.back.domain.chat.chat.dto.ChatRoomDto;
import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.chat.chat.entity.Message;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import com.back.domain.chat.chat.repository.MessageRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.entity.Status;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ChatService chatService;

    // =============== createChatRoom 테스트들 ===============

    @Test
    @DisplayName("채팅방 생성 성공")
    void createChatRoom_Success() {
        // given
        Long postId = 1L;
        String userName = "testuser";

        Member mockMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .name(userName)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        Post mockPost = Post.builder()
                .member(mockMember)
                .title("테스트 게시글")
                .description("테스트 내용")
                .category(Post.Category.PRODUCT)
                .price(100000)
                .status(Post.Status.SALE)
                .build();

        when(memberRepository.findByName(userName)).thenReturn(Optional.of(mockMember));
        when(chatRoomRepository.findByPostIdAndMemberId(postId, mockMember.getId())).thenReturn(Optional.empty());
        when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(new ChatRoom(mockPost, mockMember));

        // when
        chatService.createChatRoom(postId, userName);

        // then
        verify(memberRepository, times(1)).findByName(userName);
        verify(chatRoomRepository, times(1)).findByPostIdAndMemberId(postId, mockMember.getId());
        verify(postRepository, times(1)).findById(postId);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 실패 - null userName")
    void createChatRoom_Fail_NullUserName() {
        // given
        Long postId = 1L;
        String userName = null;

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400-1 : 로그인 하셔야 합니다.");

        // verify
        verify(memberRepository, never()).findByName(any());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 빈 userName")
    void createChatRoom_Fail_EmptyUserName() {
        // given
        Long postId = 1L;
        String userName = "";

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400-1 : 로그인 하셔야 합니다.");

        // verify
        verify(memberRepository, never()).findByName(any());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 존재하지 않는 사용자")
    void createChatRoom_Fail_MemberNotFound() {
        // given
        Long postId = 1L;
        String userName = "nonexistentuser";

        when(memberRepository.findByName(userName)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404-3 : 존재하지 않는 사용자입니다.");

        // verify
        verify(memberRepository, times(1)).findByName(userName);
        verify(chatRoomRepository, never()).findByPostIdAndMemberId(any(), any());
        verify(postRepository, never()).findById(any());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 이미 생성된 채팅방 존재")
    void createChatRoom_Fail_ChatRoomAlreadyExists() {
        // given
        Long postId = 1L;
        String userName = "testuser";

        Member mockMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .name(userName)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        ChatRoom existingChatRoom = mock(ChatRoom.class);

        when(memberRepository.findByName(userName)).thenReturn(Optional.of(mockMember));
        when(chatRoomRepository.findByPostIdAndMemberId(postId, mockMember.getId()))
                .thenReturn(Optional.of(existingChatRoom));

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("409-1 : 이미 생성된 채팅방이 있습니다.");

        // verify
        verify(memberRepository, times(1)).findByName(userName);
        verify(chatRoomRepository, times(1)).findByPostIdAndMemberId(postId, mockMember.getId());
        verify(postRepository, never()).findById(any());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 존재하지 않는 게시글")
    void createChatRoom_Fail_PostNotFound() {
        // given
        Long postId = 999L;
        String userName = "testuser";

        Member mockMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .name(userName)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        when(memberRepository.findByName(userName)).thenReturn(Optional.of(mockMember));
        when(chatRoomRepository.findByPostIdAndMemberId(postId, mockMember.getId())).thenReturn(Optional.empty());
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404-1 : 존재하지 않는 게시글입니다.");

        // verify
        verify(memberRepository, times(1)).findByName(userName);
        verify(chatRoomRepository, times(1)).findByPostIdAndMemberId(postId, mockMember.getId());
        verify(postRepository, times(1)).findById(postId);
        verify(chatRoomRepository, never()).save(any());
    }

    // =============== getMyChatRooms 테스트들 ===============

    @Test
    @DisplayName("내 채팅방 목록 조회 성공 - 메시지가 있는 경우")
    void getMyChatRooms_Success_WithMessages() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        String userName = "testuser";

        Member mockMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .name(userName)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        Post mockPost1 = Post.builder()
                .member(mockMember)
                .title("게시글1")
                .description("내용1")
                .category(Post.Category.PRODUCT)
                .price(100000)
                .status(Post.Status.SALE)
                .build();

        Post mockPost2 = Post.builder()
                .member(mockMember)
                .title("게시글2")
                .description("내용2")
                .category(Post.Category.METHOD)
                .price(200000)
                .status(Post.Status.SALE)
                .build();

        ChatRoom mockChatRoom1 = new ChatRoom(mockPost1, mockMember);
        ChatRoom mockChatRoom2 = new ChatRoom(mockPost2, mockMember);

        Message mockMessage1 = mock(Message.class);
        Message mockMessage2 = mock(Message.class);

        when(mockPrincipal.getName()).thenReturn(userName);
        when(memberRepository.findByName(userName)).thenReturn(Optional.of(mockMember));
        when(chatRoomRepository.findByMemberIdOrderByCreatedAtDesc(mockMember.getId()))
                .thenReturn(Arrays.asList(mockChatRoom1, mockChatRoom2));
        when(messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(mockMessage1, mockMessage2);
        when(mockMessage1.getContent()).thenReturn("안녕하세요!");
        when(mockMessage2.getContent()).thenReturn("반갑습니다.");

        // when
        List<ChatRoomDto> result = chatService.getMyChatRooms(mockPrincipal);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).lastContent()).isEqualTo("안녕하세요!");
        assertThat(result.get(1).lastContent()).isEqualTo("반갑습니다.");

        // verify
        verify(memberRepository, times(1)).findByName(userName);
        verify(chatRoomRepository, times(1)).findByMemberIdOrderByCreatedAtDesc(mockMember.getId());
        verify(messageRepository, times(2)).findFirstByChatRoomIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 성공 - 메시지가 없는 경우")
    void getMyChatRooms_Success_WithoutMessages() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        String userName = "testuser";

        Member mockMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .name(userName)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        Post mockPost = Post.builder()
                .member(mockMember)
                .title("게시글")
                .description("내용")
                .category(Post.Category.PRODUCT)
                .price(100000)
                .status(Post.Status.SALE)
                .build();

        ChatRoom mockChatRoom = new ChatRoom(mockPost, mockMember);

        when(mockPrincipal.getName()).thenReturn(userName);
        when(memberRepository.findByName(userName)).thenReturn(Optional.of(mockMember));
        when(chatRoomRepository.findByMemberIdOrderByCreatedAtDesc(mockMember.getId()))
                .thenReturn(Arrays.asList(mockChatRoom));
        when(messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(null);

        // when
        List<ChatRoomDto> result = chatService.getMyChatRooms(mockPrincipal);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).lastContent()).isEqualTo("메시지가 없습니다.");

        // verify
        verify(memberRepository, times(1)).findByName(userName);
        verify(chatRoomRepository, times(1)).findByMemberIdOrderByCreatedAtDesc(mockMember.getId());
        verify(messageRepository, times(1)).findFirstByChatRoomIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 실패 - null Principal")
    void getMyChatRooms_Fail_NullPrincipal() {
        // when & then
        assertThatThrownBy(() -> chatService.getMyChatRooms(null))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400-1 : 로그인 하셔야 합니다.");

        // verify
        verify(memberRepository, never()).findByName(any());
        verify(chatRoomRepository, never()).findByMemberIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 실패 - 빈 userName")
    void getMyChatRooms_Fail_EmptyUserName() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("");

        // when & then
        assertThatThrownBy(() -> chatService.getMyChatRooms(mockPrincipal))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400-1 : 로그인 하셔야 합니다.");

        // verify
        verify(memberRepository, never()).findByName(any());
        verify(chatRoomRepository, never()).findByMemberIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 실패 - 존재하지 않는 사용자")
    void getMyChatRooms_Fail_MemberNotFound() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        String userName = "nonexistentuser";

        when(mockPrincipal.getName()).thenReturn(userName);
        when(memberRepository.findByName(userName)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.getMyChatRooms(mockPrincipal))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404-3 : 존재하지 않는 사용자입니다.");

        // verify
        verify(memberRepository, times(1)).findByName(userName);
        verify(chatRoomRepository, never()).findByMemberIdOrderByCreatedAtDesc(any());
    }

    // =============== deleteChatRoom 테스트들 ===============

    @Test
    @DisplayName("채팅방 삭제 성공")
    void deleteChatRoom_Success() {
        // given
        Long chatRoomId = 1L;
        ChatRoom mockChatRoom = mock(ChatRoom.class);

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(mockChatRoom));

        // when
        chatService.deleteChatRoom(chatRoomId);

        // then
        verify(chatRoomRepository, times(1)).findById(chatRoomId);
        verify(chatRoomRepository, times(1)).delete(mockChatRoom);
    }

    @Test
    @DisplayName("채팅방 삭제 실패 - 존재하지 않는 채팅방")
    void deleteChatRoom_Fail_ChatRoomNotFound() {
        // given
        Long chatRoomId = 999L;

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.deleteChatRoom(chatRoomId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404-4 : 존재하지 않는 채팅방입니다.");

        // verify
        verify(chatRoomRepository, times(1)).findById(chatRoomId);
        verify(chatRoomRepository, never()).delete(any());
    }

    // =============== getChatRoomMessages 테스트들 ===============

    @Test
    @DisplayName("채팅방 메시지 조회 성공")
    void getChatRoomMessages_Success() {
        // given
        Long chatRoomId = 1L;

        Member mockSender = Member.builder()
                .email("sender@test.com")
                .password("password")
                .name("발신자")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        ChatRoom mockChatRoom = mock(ChatRoom.class);
        Message mockMessage1 = mock(Message.class);
        Message mockMessage2 = mock(Message.class);

        when(chatRoomRepository.existsById(chatRoomId)).thenReturn(true);
        when(messageRepository.findByChatRoomId(chatRoomId))
                .thenReturn(Arrays.asList(mockMessage1, mockMessage2));

        // Message 모킹
        when(mockMessage1.getSender()).thenReturn(mockSender);
        when(mockMessage1.getChatRoom()).thenReturn(mockChatRoom);
        when(mockMessage1.getContent()).thenReturn("안녕하세요");
        when(mockMessage1.getCreatedAt()).thenReturn(java.time.LocalDateTime.now().minusMinutes(5));

        when(mockMessage2.getSender()).thenReturn(mockSender);
        when(mockMessage2.getChatRoom()).thenReturn(mockChatRoom);
        when(mockMessage2.getContent()).thenReturn("반갑습니다");
        when(mockMessage2.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());

        when(mockChatRoom.getId()).thenReturn(chatRoomId);

        // when
        List<MessageDto> result = chatService.getChatRoomMessages(chatRoomId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("안녕하세요");
        assertThat(result.get(1).getContent()).isEqualTo("반갑습니다");

        // verify
        verify(chatRoomRepository, times(1)).existsById(chatRoomId);
        verify(messageRepository, times(1)).findByChatRoomId(chatRoomId);
    }

    @Test
    @DisplayName("채팅방 메시지 조회 실패 - 존재하지 않는 채팅방")
    void getChatRoomMessages_Fail_ChatRoomNotFound() {
        // given
        Long chatRoomId = 999L;

        when(chatRoomRepository.existsById(chatRoomId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> chatService.getChatRoomMessages(chatRoomId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404-4 : 존재하지 않는 채팅방입니다.");

        // verify
        verify(chatRoomRepository, times(1)).existsById(chatRoomId);
        verify(messageRepository, never()).findByChatRoomId(any());
    }
}
