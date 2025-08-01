package com.back.domain.post.controller;

import com.back.domain.post.dto.FavoriteResponseDTO;
import com.back.domain.post.service.PostService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class FavoriteController {

    private final PostService postService;

    @Operation(summary = "찜 등록, 해제", description = "이미 찜한 경우 해제, 찜하지 않은 경우 등록")
    @PostMapping("/{postId}")
    public FavoriteResponseDTO toggleFavorite(@PathVariable Long postId) {
        return postService.toggleFavorite(postId);
    }


}

