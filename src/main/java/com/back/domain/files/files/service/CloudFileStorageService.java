// CloudFileStorageService.java
package com.back.domain.files.files.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@Profile("prod")
public class CloudFileStorageService implements FileStorageService {

//     === 클라우드 스토리지 설정 (예시) ===
//     @Value("${cloud.storage.bucket-name}")
//     private String bucketName;
//
//     private final Storage gcsStorage; // 또는 AmazonS3 s3Client;
//
//     public CloudFileStorageService(Storage gcsStorage) { // 또는 AmazonS3 s3Client
//         this.gcsStorage = gcsStorage;
//     }

    @Override
    public String storeFile(MultipartFile file, String subFolder) {
        // 파일 크기 제한 (예: 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        }

        try {
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedFileType(contentType)) {
                throw new RuntimeException("허용되지 않는 파일 형식입니다.");
            }

            // 실제 클라우드 스토리지 (GCS, S3 등)에 파일 저장 로직 구현
            // 예시:
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getExtension(originalFileName);
            String storedFileName = "post_" + subFolder + "/" + UUID.randomUUID() + fileExtension; // 변경: 클라우드 스토리지용 경로 구성 예시

            // GCS 예시:
            // BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, storedFileName)
            //                             .setContentType(file.getContentType())
            //                             .build();
            // gcsStorage.create(blobInfo, file.getBytes());
            // return String.format("https://storage.googleapis.com/%s/%s", bucketName, storedFileName);

            // AWS S3 예시:
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setContentLength(file.getSize());
            // metadata.setContentType(file.getContentType());
            // s3Client.putObject(bucketName, storedFileName, file.getInputStream(), metadata);
            // return s3Client.getUrl(bucketName, storedFileName).toString();

            log.info("클라우드 스토리지에 파일 저장 시뮬레이션: {}", storedFileName); // 임시 로그
            return "https://your-cloud-storage.com/" + storedFileName; // 변경: 클라우드 URL 형식으로 반환
        } catch (Exception e) { // IOException 대신 상위 Exception으로 잡을 수 있습니다.
            log.error("클라우드 스토리지 파일 저장 실패", e);
            throw new RuntimeException("클라우드 스토리지 파일 저장 중 오류가 발생했습니다.");
        }
    }

    @Override // 추가: 인터페이스 구현 명시
    public void deletePhysicalFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("삭제할 파일 URL이 비어있습니다.");
            return;
        }

        // 예시:
        // if (fileUrl == null || !fileUrl.startsWith("https://your-cloud-storage.com/")) return;
        // if (!fileUrl.startsWith("https://your-cloud-storage.com/")) {
        //     log.warn("유효하지 않은 클라우드 스토리지 URL: {}", fileUrl);
        //     return;
        // }
        // String fileNameToDelete = fileUrl.substring("https://your-cloud-storage.com/".length());
        // gcsStorage.delete(BlobId.of(bucketName, fileNameToDelete)); // GCS 예시
        // s3Client.deleteObject(bucketName, fileNameToDelete); // S3 예시

        log.info("클라우드 스토리지에서 파일 삭제 시뮬레이션: {}", fileUrl); // 임시 로그
    }

    private String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex != -1 ? fileName.substring(dotIndex) : "";
    }

    // 허용하는 파일 타입
    private boolean isAllowedFileType(String contentType) {
        return contentType.startsWith("image/") ||
                contentType.equals("application/pdf") ||
                contentType.startsWith("text/");
    }
}