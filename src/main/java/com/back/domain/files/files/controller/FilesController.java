package com.back.domain.files.files.controller;

import com.back.domain.files.files.dto.FileUploadResponseDto;
import com.back.domain.files.files.service.FilesService;
import com.back.global.rsData.RsData;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class FilesController {

    private final FilesService filesService;

    // 파일 업로드 (게시글 저장 -> postId 받음, 이미지 저장)
    @PostMapping("/{postId}/files")
    public RsData<List<FileUploadResponseDto>> uploadFiles(
            @PathVariable @Positive long postId,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return filesService.uploadFiles(postId, files);
    }

    // 파일 조회
    @GetMapping("/{postId}/files")
    public RsData<List<FileUploadResponseDto>> getFilesByPostId(@PathVariable Long postId) {
        return filesService.getFilesByPostId(postId);
    }

}
