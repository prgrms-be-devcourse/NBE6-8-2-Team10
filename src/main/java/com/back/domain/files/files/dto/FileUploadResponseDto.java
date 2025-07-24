package com.back.domain.files.files.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileUploadResponseDto {
    // 파일 업로드 응답 DTO
    private Long id;
    private Long postId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String fileUrl;
    private int sortOrder;
    private LocalDateTime createdAt;
}
