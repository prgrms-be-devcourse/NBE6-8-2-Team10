// FilesService.java
package com.back.domain.files.files.service;

import com.back.domain.files.files.dto.FileUploadResponseDto;
import com.back.domain.files.files.entity.Files;
import com.back.domain.files.files.repository.FilesRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FilesService {

    private final FilesRepository filesRepository;
    private final FileStorageService fileStorageService; // 변경: LocalFileStorageService 대신 인터페이스 주입
    private final PostRepository postRepository;
    private final Rq rq;

    // 파일 업로드 서비스
    public RsData<List<FileUploadResponseDto>> uploadFiles(Long postId, MultipartFile[] files) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다: " + postId));

        // 게시글 작성자와 요청자의 일치 여부 확인
        if (rq.getMember() == null || !rq.getMember().getId().equals(post.getMember().getId())) {
            throw new IllegalArgumentException("게시글 작성자만 파일을 업로드할 수 있습니다.");
        }

        List<FileUploadResponseDto> responseList = new ArrayList<>();
        int sortOrder = 1;

        if(files != null) {
            for (MultipartFile file : files) {
                // 파일이 없는 경우 건너뜀
                if (file.isEmpty()) {continue;}

                // 파일명 검사
                String fileName = file.getOriginalFilename();
                if (fileName == null || fileName.trim().isEmpty()) {continue;}

                String fileType = file.getContentType();
                long fileSize = file.getSize();

                // 파일 객체 스토리지에 저장
                String fileUrl = null; // 초기화
                try {
                    fileUrl = fileStorageService.storeFile(file, "post_" + postId);
                } catch (RuntimeException e) {
                    log.error("파일 저장 실패, 건너뜀: " + fileName, e);
                    continue;
                }

                // 파일 메타데이터 저장
                Files saved = filesRepository.save(
                        Files.builder()
                                .post(post)
                                .fileName(fileName)
                                .fileType(fileType)
                                .fileSize(fileSize)
                                .fileUrl(fileUrl) // 저장된 fileUrl 사용
                                .sortOrder(sortOrder++)
                                .build()
                );

                responseList.add(FileUploadResponseDto.from(saved));
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
                .map(FileUploadResponseDto::from)
                .toList();

        return new RsData<>(
                "200",
                result.isEmpty() ? "첨부된 파일이 없습니다." : "파일 목록 조회 성공",
                result
        );
    }

    // 파일 개별 삭제 서비스
    public RsData<Void> deleteFile(Long postId, Long fileId) {

        Files file = filesRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다: " + fileId));

        if (!file.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글에 속하지 않는 파일입니다: " + fileId);
        }

        // 로그인 확인
        if (!rq.isLogin()) {
            throw new IllegalArgumentException("로그인 후 이용해 주세요.");
        }

        // ID가져와 권한 확인
        Long currentMemberId = rq.getMemberId();
        if (!file.getPost().getMember().getId().equals(currentMemberId)) {
            throw new IllegalArgumentException("해당 파일을 삭제할 권한이 없습니다. 현재 사용자 ID: " + currentMemberId);
        }

        // 데이터베이스에서 파일 메타데이터 삭제 전, 물리 파일 삭제 시도
        deletePhysicalFileSafely(file.getFileUrl());

        filesRepository.deleteById(fileId);
        return new RsData("200", "파일 삭제 성공", null);
    }

    // =================== 관리자 전용 서비스 구역 ===================

    // 모든 파일 조회
    public RsData<Page<FileUploadResponseDto>> adminGetAllFiles(Pageable pageable) {
        // 관리자 권한 확인
        if (!rq.isAdmin()) {
            return new RsData<>("403-1", "관리자 권한이 필요합니다.", null);
        }

        Page<Files> filesPage = filesRepository.findAll(pageable);

        Page<FileUploadResponseDto> dtoPage = filesPage.map(FileUploadResponseDto::from);

        return new RsData<>("200", dtoPage.isEmpty() ? "등록된 파일이 없습니다." : "파일 목록 조회 성공", dtoPage);
    }

    // 파일 개별 조회(관리자)
    public RsData<FileUploadResponseDto> adminGetFileById(Long fileId) {
        // 관리자 확인
        if (!rq.isAdmin()) {
            return new RsData<>("403-2", "관리자 권한이 필요합니다.", null);
        }
        // 파일 ID로 파일 조회
        Files file = filesRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다: " + fileId));

        return new RsData<>("200", "파일 조회 성공 (관리자)", FileUploadResponseDto.from(file));
    }

    // 파일 삭제(관리자)
    public RsData<Void> adminDeleteFile(Long fileId) {
        // 관리자 권한 확인
        if (!rq.isAdmin()) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }

        // 파일 ID로 파일 조회
        Files file = filesRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다. " + fileId));

        // 물리 파일 삭제
        deletePhysicalFileSafely(file.getFileUrl());

        // 데이터베이스에서 파일 메타데이터 삭제
        filesRepository.deleteById(fileId);
        return new RsData<>("200", "파일 삭제 성공 (관리자)", null);
    }


    // ==============헬퍼 메서드 영역 ==============
    // 물리 파일 삭제
    private void deletePhysicalFileSafely(String fileUrl) {
        try {
            fileStorageService.deletePhysicalFile(fileUrl);
        } catch (Exception e) {
            log.error("물리 파일 삭제 중 오류 발생: " + fileUrl, e);
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }
}