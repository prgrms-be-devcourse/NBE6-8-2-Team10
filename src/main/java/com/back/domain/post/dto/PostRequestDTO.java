package com.back.domain.post.dto;

//게시글 등록, 수정 요청용
public record PostRequestDTO(
        String title,
        String description,
        String category,  // enum 문자열 (예: "PRODUCT", "COPYRIGHT")
        String fileUrl,
        int price
) {}
