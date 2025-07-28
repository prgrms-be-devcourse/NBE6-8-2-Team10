package com.back.domain.chat.chat.service;

import com.back.domain.chat.chat.entity.ChatRoom;
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
    void createChatRoom_Fail_DatabaseError() {
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
}
