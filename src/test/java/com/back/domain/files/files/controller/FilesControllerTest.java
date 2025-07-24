package com.back.domain.files.files.controller;

import com.back.domain.files.files.dto.FileUploadResponseDto;
import com.back.domain.files.files.service.FilesService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

        given(filesService.uploadFiles(eq(5L), any(MultipartFile[].class))).willReturn(response);

        mockMvc.perform(multipart("/api/posts/5/files")
                .file(file1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("파일 업로드 성공"))
                .andExpect(jsonPath("$.data[0].fileName").value("test1.png"));
    }
}
