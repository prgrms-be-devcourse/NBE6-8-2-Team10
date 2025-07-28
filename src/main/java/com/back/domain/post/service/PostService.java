package com.back.domain.post.service;

import com.back.domain.post.dto.PostRequestDTO;
import com.back.domain.post.dto.PostDetailDTO;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public PostDetailDTO createPost(PostRequestDTO dto) {
        // 카테고리 변환 예외 처리
        Post.Category category = Post.Category.from(dto.category())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        Post post = Post.builder()
                .title(dto.title())
                .description(dto.description())
                .category(category)
                .price(dto.price())
                .status(Post.Status.SALE)
                .build();

        Post saved = postRepository.save(post);
        return new PostDetailDTO(saved, false);
    }
}
