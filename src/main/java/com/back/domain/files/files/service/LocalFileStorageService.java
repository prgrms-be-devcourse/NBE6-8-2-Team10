package com.back.domain.files.files.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@Profile("dev")
public class LocalFileStorageService implements FileStorageService {

    // === 로컬 저장 경로 ===
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 첨부파일 저장
    @Override
    public String storeFile(MultipartFile file, String subFolder) {
        // 파일 크기 제한 (예: 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        }

        try {
            // 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir, subFolder);
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            // 저장된 파일명 생성
            String originalFileName = file.getOriginalFilename();
            String extension = getExtension(originalFileName);

            // 파일명 생성
            String fileName = "file_" + System.currentTimeMillis() + "_" +
                    UUID.randomUUID().toString().substring(0, 8) + extension;

            Path filePath = uploadPath.resolve(fileName);
            // 파일명 중복 시 재시도
            int counter = 1;
            while(Files.exists(filePath)){
                String newFileName = "file_" + System.currentTimeMillis() + "_" +
                        UUID.randomUUID().toString().substring(0, 8) + "_" + (counter++) + extension;
                filePath = uploadPath.resolve(newFileName);
            }

            file.transferTo(filePath.toFile());

            // 접근 가능한 URL 반환
            return "file:///" + filePath.toAbsolutePath().toString().replace("\\", "/");

        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
        }
    }

    // 파일 삭제
    public void deletePhysicalFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith("file:///")) {
                log.warn("로컬 파일 삭제: 유효하지 않는 URL 형식입니다." + fileUrl);
                return;
            }

            String localPath = java.net.URLDecoder.decode(fileUrl.replace("file:///", ""), "UTF-8");
            File file = new File(localPath);
            if (file.exists()) {
                if (!file.delete()) {
                    log.warn("파일 삭제 실패: " + localPath);
                } else {
                    log.info("로컬 파일 삭제 성공: " + localPath);
                }
            } else {
                log.info("로컬 파일이 존재하지 않아 삭제할 수 없습니다: " + localPath);
            }
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생", e);
        }
    }

    // 파일 확장자 추출
    private String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex != -1 ? fileName.substring(dotIndex) : "";
    }
}
