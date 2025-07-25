package com.back.domain.post.controller;

import com.back.domain.post.dto.PostDetailDTO;
import com.back.domain.post.dto.PostRequestDTO;
import com.back.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

@PostMapping
public ResponseEntity<PostDetailDTO> createPost(@Valid @RequestBody PostRequestDTO dto) {
    PostDetailDTO result = postService.createPost(dto);
    URI location = URI.create("/api/posts/" + result.id());
    return ResponseEntity.created(location).body(result);
    }
}
