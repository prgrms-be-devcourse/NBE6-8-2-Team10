package com.back.domain.files.files.controller;

import com.back.domain.files.files.dto.FileUploadResponseDto;
import com.back.domain.files.files.service.FilesService;
import com.back.domain.member.service.MemberService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class FilesController {

    private final FilesService filesService;
    private final MemberService memberService;

    // 파일 업로드 (게시글 저장 -> postId 받음, 이미지 저장)
    @PostMapping(value = "/{postId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RsData<List<FileUploadResponseDto>> uploadFiles(
            @PathVariable @Positive long postId,
            @Parameter (description = "업로드할 파일들", required = false)
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return filesService.uploadFiles(postId, files);
    }

    // 파일 조회
    @GetMapping("/{postId}/files")
    public RsData<List<FileUploadResponseDto>> getFilesByPostId(@PathVariable Long postId) {
        return filesService.getFilesByPostId(postId);
    }

    // 파일 삭제
    @DeleteMapping("/{postId}/files/{fileId}")
    public RsData<Void> deleteFile(
            @PathVariable @Positive long postId,
            @PathVariable @Positive long fileId

    ) {
        Long memberId = 1L; // 테스트
        return filesService.deleteFile(postId, fileId, memberId);
    }
}
