package com.back.domain.files.files.dto;

import java.time.LocalDateTime;

public record FileUploadResponseDto (
    // 파일 업로드 응답 DTO
     Long id,
     Long postId,
     String fileName,
     String fileType,
     long fileSize,
     String fileUrl,
     int sortOrder,
     LocalDateTime createdAt
) {}
