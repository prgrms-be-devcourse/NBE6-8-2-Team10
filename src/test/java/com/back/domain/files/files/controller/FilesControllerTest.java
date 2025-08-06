package com.back.domain.files.files.controller;

import com.back.domain.files.files.dto.FileUploadResponseDto;
import com.back.domain.files.files.service.FilesService;
import com.back.global.rsData.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FilesService filesService;

    @Test
    @DisplayName("파일 업로드 성공")
    @WithMockUser(username = "test-user", roles = "USER")
    void t1() throws Exception {
        // 주입
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "test1.png", "image/png", "fake-image-content".getBytes());

        List<FileUploadResponseDto> response = List.of(
                new FileUploadResponseDto(
                        1L,
                        5L,
                        "test1.png",
                        "image/png",
                        20L,
                        "http://example.com/uploads/test1.png",
                        1,
                        LocalDateTime.now()
                )
        );

        RsData<List<FileUploadResponseDto>> rsData = new RsData<>(
                "200",
                "파일 업로드 성공",
                response
        );

        given(filesService.uploadFiles(eq(5L), any(MultipartFile[].class))).willReturn(rsData);

        mockMvc.perform(multipart("/api/posts/5/files")
                .file(file1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("파일 업로드 성공"))
                .andExpect(jsonPath("$.data[0].fileName").value("test1.png"));
    }

    @Test
    @DisplayName("파일 없이 업로드 - 첨부된 파일 없음")
    @WithMockUser(username = "test-user", roles = "USER")
    void t2() throws Exception {
        // 빈 응답 데이터
        List<FileUploadResponseDto> emptyResponse = List.of();

        // RsData 응답 구성
        RsData<List<FileUploadResponseDto>> rsData = new RsData<>(
                "200",
                "첨부된 파일이 없습니다",
                emptyResponse
        );

        given(filesService.uploadFiles(eq(5L), any(MultipartFile[].class))).willReturn(rsData);

        // 실제 요청 (파일 없음)
        mockMvc.perform(multipart("/api/posts/5/files")
                        .file(new MockMultipartFile("files", new byte[0]))) // 빈 파일
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("첨부된 파일이 없습니다"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @DisplayName("파일 조회 성공 - 파일 있음")
    @WithMockUser(username = "test-user", roles = "USER")
    void t3() throws Exception {
        long postId = 5L;

        List<FileUploadResponseDto> fileList = List.of(
                new FileUploadResponseDto(
                        1L,
                        postId,
                        "test1.png",
                        "image/png",
                        20L,
                        "http://example.com/uploads/test1.png",
                        1,
                        LocalDateTime.now()
                )
        );

        RsData<List<FileUploadResponseDto>> rsData = new RsData<>(
                "200",
                "파일 목록 조회 성공",
                fileList
        );

        given(filesService.getFilesByPostId(postId)).willReturn(rsData);

        mockMvc.perform(get("/api/posts/{postId}/files", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("파일 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fileName").value("test1.png"));
    }

    @Test
    @DisplayName("파일 조회 - 파일 없음(빈 파일, 정상)")
    @WithMockUser(username = "test-user", roles = "USER")
    void t4() throws Exception {
        long postId = 5L;

        RsData<List<FileUploadResponseDto>> rsData = new RsData<>(
                "200",
                "첨부된 파일이 없습니다.",
                List.of()
        );

        given(filesService.getFilesByPostId(postId)).willReturn(rsData);

        mockMvc.perform(get("/api/posts/{postId}/files", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("첨부된 파일이 없습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("파일 삭제 성공")
    @WithMockUser(username = "test-user", roles = "USER")
    void t5() throws Exception {
        long postId = 5L;
        long fileId = 1L;

        RsData<Void> rsData = new RsData<>(
                "200",
                "파일 삭제 성공",
                null
        );

        // filesService.deleteFile 호출 시 응답 설정
        given(filesService.deleteFile(postId, fileId)).willReturn(rsData); // memberId는 1L로 고정됨

        mockMvc.perform(delete("/api/posts/{postId}/files/{fileId}", postId, fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("파일 삭제 성공"));
    }

}
