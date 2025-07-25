package com.back.domain.files.files.service;

import com.back.domain.files.files.dto.FileUploadResponseDto;
import com.back.domain.files.files.entity.Files;
import com.back.domain.files.files.repository.FilesRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FilesService {

    private final FilesRepository filesRepository;
    private final PostRepository postRepository;

    // 파일 업로드 서비스
    public RsData<List<FileUploadResponseDto>> uploadFiles(Long postId, MultipartFile[] files) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다: " + postId));

        List<FileUploadResponseDto> responseList = new ArrayList<>();

        if(files != null) {
            int sortOrder = 1;
            for (MultipartFile file : files) {
                // 파일이 없는 경우 건너뜀
                if (file.isEmpty()) {continue;}

                // 파일명 검사
                String fileName = file.getOriginalFilename();
                if (fileName == null || fileName.trim().isEmpty()) {continue;}

                String fileType = file.getContentType();
                long fileSize = file.getSize();

                // TODO: 실제 파일 저장 후 URL 생성(임시 URL 사용)
                String fileUrl = "http://example.com/uploads/test.png" + fileName;

                Files saved = filesRepository.save(
                        Files.builder()
                                .post(post)
                                .fileName(fileName)
                                .fileType(fileType)
                                .fileSize(fileSize)
                                .fileUrl(fileUrl)
                                .sortOrder(sortOrder++)
                                .build()
                );

                responseList.add(new FileUploadResponseDto(
                        saved.getId(),
                        post.getId(),
                        saved.getFileName(),
                        saved.getFileType(),
                        saved.getFileSize(),
                        saved.getFileUrl(),
                        saved.getSortOrder(),
                        saved.getCreatedAt()
                ));
            }
        }
        return new RsData<>(
                "200",
                responseList.isEmpty() ? "첨부된 파일이 없습니다." : "파일 업로드 성공",
                responseList
        );
    }

    // 게시글 ID로 파일 조회 서비스
    public RsData<List<FileUploadResponseDto>> getFilesByPostId(Long postId) {
        List<Files> files = filesRepository.findByPostIdOrderBySortOrderAsc(postId);

        List<FileUploadResponseDto> result = files.stream()
                .map(file -> new FileUploadResponseDto(
                        file.getId(),
                        file.getPost().getId(),
                        file.getFileName(),
                        file.getFileType(),
                        file.getFileSize(),
                        file.getFileUrl(),
                        file.getSortOrder(),
                        file.getCreatedAt()
                ))
                .toList();

        return new RsData<>(
                "200",
                result.isEmpty() ? "첨부된 파일이 없습니다." : "파일 목록 조회 성공",
                result
        );
    }
}
