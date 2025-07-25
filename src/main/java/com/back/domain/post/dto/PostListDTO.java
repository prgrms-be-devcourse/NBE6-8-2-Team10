package com.back.domain.post.dto;

import com.back.domain.post.entity.Post;
import java.time.LocalDateTime;

//게시글 목록 응답용
public record PostListDTO(
        long id,
        String title,
        int price,
        String category,
        int favoriteCnt,
        LocalDateTime createdAt
) {
    public PostListDTO(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getPrice(),
                post.getCategory().getLabel(),  // enum → 한글
                post.getFavoriteCnt(),
                post.getCreatedAt()
        );
    }
}