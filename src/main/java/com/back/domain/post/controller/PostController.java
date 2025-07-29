package com.back.domain.post.controller;

import com.back.domain.post.dto.PostDetailDTO;
import com.back.domain.post.dto.PostListDTO;
import com.back.domain.post.dto.PostRequestDTO;
import com.back.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    //게시글 등록
    @PostMapping
    public ResponseEntity<PostDetailDTO> createPost(@Valid @RequestBody PostRequestDTO dto) {
        PostDetailDTO result = postService.createPost(dto);
        URI location = URI.create("/api/posts/" + result.id());
        return ResponseEntity.created(location).body(result);
        }

    //인기 게시글 top 10 보여줌
    @GetMapping("/popular")
    public ResponseEntity<List<PostListDTO>> getTop10PopularPosts() {
        List<PostListDTO> result = postService.getTop10PopularPosts();
        return ResponseEntity.ok(result);
    }

}
