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

    // 파일 업로드 API(게시글 저장 -> postId 받음, 이미지 저장)
    @PostMapping("/{postId}/files")
    public RsData<List<FileUploadResponseDto>> uploadFiles(
            @PathVariable @Positive long postId,
            @RequestPart("files") MultipartFile[] files
    ) {
        try {
            List<FileUploadResponseDto> response = filesService.uploadFiles(postId, files);
            return new RsData<>("200", "파일 업로드 성공", response);
        } catch (Exception e) {
            return new RsData<>("500", "파일 업로드 실패: " + e.getMessage(), null);
        }
    }
}
