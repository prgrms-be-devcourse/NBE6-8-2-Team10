package com.back.domain.post.dto;

import com.back.domain.post.entity.FavoritePost;
import java.time.LocalDateTime;

//찜 목록 응답용
public record FavoritePostDTO(
        long postId,
        String title,
        int price,
        String fileUrl,
        int favoriteCnt,
        String status,
        LocalDateTime createdAt
) {
    public FavoritePostDTO(FavoritePost favoritePost) {
        this(
                favoritePost.getPost().getId(),
                favoritePost.getPost().getTitle(),
                favoritePost.getPost().getPrice(),
                favoritePost.getPost().getFileUrl(),
                favoritePost.getPost().getFavoriteCnt(),
                favoritePost.getPost().getStatus().getLabel(),
                favoritePost.getPost().getCreatedAt()
        );
    }
}


