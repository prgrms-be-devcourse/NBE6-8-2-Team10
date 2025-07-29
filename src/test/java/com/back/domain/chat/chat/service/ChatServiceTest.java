package com.back.domain.chat.chat.service;

import com.back.domain.chat.chat.dto.ChatRoomDto;
import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.chat.chat.entity.Message;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import com.back.domain.chat.chat.repository.MessageRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    @DisplayName("채팅방 생성 성공")
    void createChatRoom_Success() {
        // given
        Long postId = 1L;
        String userName = "testuser";

        Post mockPost = mock(Post.class);
        ChatRoom mockChatRoom = mock(ChatRoom.class);

        when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);

        // when
        chatService.createChatRoom(postId, userName);

        // then
        verify(postRepository, times(1)).findById(postId);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 존재하지 않는 게시글")
    void createChatRoom_Fail_PostNotFound() {
        // given
        Long postId = 999L;
        String userName = "testuser";

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404-1 : 존재하지 않는 게시글입니다.");

        // verify
        verify(postRepository, times(1)).findById(postId);
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 시 올바른 데이터로 ChatRoom 생성")
    void createChatRoom_CorrectData() {
        // given
        Long postId = 1L;
        String userName = "testuser";

        Post mockPost = mock(Post.class);

        when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom chatRoom = invocation.getArgument(0);

            // 저장되는 ChatRoom 객체의 필드 검증
            assertThat(chatRoom.getPost()).isEqualTo(mockPost);
            assertThat(chatRoom.getName()).isEqualTo(userName);

            return chatRoom;
        });

        // when
        chatService.createChatRoom(postId, userName);

        // then
        verify(postRepository, times(1)).findById(postId);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 실패 - null postId")
    void createChatRoom_Fail_NullPostId() {
        // given
        Long postId = null;
        String userName = "testuser";

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404-1 : 존재하지 않는 게시글입니다.");

        // verify
        verify(postRepository, times(1)).findById(postId);
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 로그인 필요")
    void createChatRoom_EmptyUserName() {
        // given
        Long postId = 1L;
        String userName = "";

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400-1 : 로그인 하셔야 합니다.");

        // verify - userName 검증에서 실패하므로 postRepository는 호출되지 않음
        verify(postRepository, never()).findById(any());
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 로그인 필요")
    void createChatRoom_Fail_needToLogin() {

        // given
        Long postId = 1L;
        String userName = "testuser";

        Post mockPost = mock(Post.class);

        when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));
        when(chatRoomRepository.save(any(ChatRoom.class)))
                .thenThrow(new ServiceException("400-1", "로그인 하셔야 합니다."));

        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(postId, userName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400-1 : 로그인 하셔야 합니다.");

        // verify
        verify(postRepository, times(1)).findById(postId);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    // =============== getMyChatRooms 테스트들 ===============

    @Test
    @DisplayName("내 채팅방 목록 조회 성공 - 메시지가 있는 경우")
    void getMyChatRooms_Success_WithMessages() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        String userName = "testuser";
        when(mockPrincipal.getName()).thenReturn(userName);

        // Mock Post
        Post mockPost1 = mock(Post.class);
        Post mockPost2 = mock(Post.class);
        when(mockPost1.getId()).thenReturn(1L);
        when(mockPost2.getId()).thenReturn(2L);

        // Mock ChatRoom
        ChatRoom mockChatRoom1 = mock(ChatRoom.class);
        ChatRoom mockChatRoom2 = mock(ChatRoom.class);
        when(mockChatRoom1.getId()).thenReturn(1L);
        when(mockChatRoom1.getName()).thenReturn(userName);
        when(mockChatRoom1.getPost()).thenReturn(mockPost1);
        when(mockChatRoom2.getId()).thenReturn(2L);
        when(mockChatRoom2.getName()).thenReturn(userName);
        when(mockChatRoom2.getPost()).thenReturn(mockPost2);

        // Mock Message
        Message mockMessage1 = mock(Message.class);
        Message mockMessage2 = mock(Message.class);
        when(mockMessage1.getContent()).thenReturn("안녕하세요!");
        when(mockMessage2.getContent()).thenReturn("반갑습니다.");

        // Mock Repository 응답
        when(chatRoomRepository.findByNameOrderByCreatedAtDesc(userName))
                .thenReturn(Arrays.asList(mockChatRoom1, mockChatRoom2));
        when(messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(1L)).thenReturn(mockMessage1);
        when(messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(2L)).thenReturn(mockMessage2);

        // when
        List<ChatRoomDto> result = chatService.getMyChatRooms(mockPrincipal);

        // then
        assertThat(result).hasSize(2);

        ChatRoomDto dto1 = result.get(0);
        assertThat(dto1.id()).isEqualTo(1L);
        assertThat(dto1.name()).isEqualTo(userName);
        assertThat(dto1.postId()).isEqualTo(1L);
        assertThat(dto1.lastContent()).isEqualTo("안녕하세요!");

        ChatRoomDto dto2 = result.get(1);
        assertThat(dto2.id()).isEqualTo(2L);
        assertThat(dto2.name()).isEqualTo(userName);
        assertThat(dto2.postId()).isEqualTo(2L);
        assertThat(dto2.lastContent()).isEqualTo("반갑습니다.");

        // verify
        verify(chatRoomRepository, times(1)).findByNameOrderByCreatedAtDesc(userName);
        verify(messageRepository, times(1)).findFirstByChatRoomIdOrderByCreatedAtDesc(1L);
        verify(messageRepository, times(1)).findFirstByChatRoomIdOrderByCreatedAtDesc(2L);
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 성공 - 메시지가 없는 경우")
    void getMyChatRooms_Success_WithoutMessages() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        String userName = "testuser";
        when(mockPrincipal.getName()).thenReturn(userName);

        // Mock Post
        Post mockPost = mock(Post.class);
        when(mockPost.getId()).thenReturn(1L);

        // Mock ChatRoom
        ChatRoom mockChatRoom = mock(ChatRoom.class);
        when(mockChatRoom.getId()).thenReturn(1L);
        when(mockChatRoom.getName()).thenReturn(userName);
        when(mockChatRoom.getPost()).thenReturn(mockPost);

        // Mock Repository 응답 - 메시지 없음
        when(chatRoomRepository.findByNameOrderByCreatedAtDesc(userName))
                .thenReturn(Arrays.asList(mockChatRoom));
        when(messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(1L)).thenReturn(null);

        // when
        List<ChatRoomDto> result = chatService.getMyChatRooms(mockPrincipal);

        // then
        assertThat(result).hasSize(1);

        ChatRoomDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo(userName);
        assertThat(dto.postId()).isEqualTo(1L);
        assertThat(dto.lastContent()).isEqualTo("메시지가 없습니다.");

        // verify
        verify(chatRoomRepository, times(1)).findByNameOrderByCreatedAtDesc(userName);
        verify(messageRepository, times(1)).findFirstByChatRoomIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 성공 - 빈 목록")
    void getMyChatRooms_Success_EmptyList() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        String userName = "testuser";
        when(mockPrincipal.getName()).thenReturn(userName);

        // Mock Repository 응답 - 빈 목록
        when(chatRoomRepository.findByNameOrderByCreatedAtDesc(userName))
                .thenReturn(Collections.emptyList());

        // when
        List<ChatRoomDto> result = chatService.getMyChatRooms(mockPrincipal);

        // then
        assertThat(result).isEmpty();

        // verify
        verify(chatRoomRepository, times(1)).findByNameOrderByCreatedAtDesc(userName);
        verify(messageRepository, never()).findFirstByChatRoomIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 실패 - null Principal")
    void getMyChatRooms_Fail_NullPrincipal() {
        // given
        Principal nullPrincipal = null;

        // when & then
        assertThatThrownBy(() -> chatService.getMyChatRooms(nullPrincipal))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400-1 : 로그인 하셔야 합니다.");

        // verify
        verify(chatRoomRepository, never()).findByNameOrderByCreatedAtDesc(any());
        verify(messageRepository, never()).findFirstByChatRoomIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 실패 - null userName")
    void getMyChatRooms_Fail_NullUserName() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> chatService.getMyChatRooms(mockPrincipal))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400-1 : 로그인 하셔야 합니다.");

        // verify
        verify(chatRoomRepository, never()).findByNameOrderByCreatedAtDesc(any());
        verify(messageRepository, never()).findFirstByChatRoomIdOrderByCreatedAtDesc(any());
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
        verify(chatRoomRepository, never()).findByNameOrderByCreatedAtDesc(any());
        verify(messageRepository, never()).findFirstByChatRoomIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 - 데이터 정렬 확인")
    void getMyChatRooms_CheckDataOrder() {
        // given
        Principal mockPrincipal = mock(Principal.class);
        String userName = "testuser";
        when(mockPrincipal.getName()).thenReturn(userName);

        // Mock Post
        Post mockPost1 = mock(Post.class);
        Post mockPost2 = mock(Post.class);
        Post mockPost3 = mock(Post.class);
        when(mockPost1.getId()).thenReturn(1L);
        when(mockPost2.getId()).thenReturn(2L);
        when(mockPost3.getId()).thenReturn(3L);

        // Mock ChatRoom (생성일시 역순으로 정렬된 상태)
        ChatRoom mockChatRoom1 = mock(ChatRoom.class); // 가장 최근
        ChatRoom mockChatRoom2 = mock(ChatRoom.class);
        ChatRoom mockChatRoom3 = mock(ChatRoom.class); // 가장 오래된

        when(mockChatRoom1.getId()).thenReturn(3L);
        when(mockChatRoom1.getName()).thenReturn(userName);
        when(mockChatRoom1.getPost()).thenReturn(mockPost3);

        when(mockChatRoom2.getId()).thenReturn(2L);
        when(mockChatRoom2.getName()).thenReturn(userName);
        when(mockChatRoom2.getPost()).thenReturn(mockPost2);

        when(mockChatRoom3.getId()).thenReturn(1L);
        when(mockChatRoom3.getName()).thenReturn(userName);
        when(mockChatRoom3.getPost()).thenReturn(mockPost1);

        // Mock Repository 응답
        when(chatRoomRepository.findByNameOrderByCreatedAtDesc(userName))
                .thenReturn(Arrays.asList(mockChatRoom1, mockChatRoom2, mockChatRoom3));
        when(messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(3L)).thenReturn(null);
        when(messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(2L)).thenReturn(null);
        when(messageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(1L)).thenReturn(null);

        // when
        List<ChatRoomDto> result = chatService.getMyChatRooms(mockPrincipal);

        // then
        assertThat(result).hasSize(3);

        // 최신 순서대로 정렬되었는지 확인
        assertThat(result.get(0).id()).isEqualTo(3L);
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(2).id()).isEqualTo(1L);

        // verify - OrderByCreatedAtDesc 메서드가 호출되었는지 확인
        verify(chatRoomRepository, times(1)).findByNameOrderByCreatedAtDesc(userName);
    }
}
