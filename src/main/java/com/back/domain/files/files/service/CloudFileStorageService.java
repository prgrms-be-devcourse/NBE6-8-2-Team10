package com.back.domain.files.files.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@Profile("prod") // 프로덕션 환경에서만 이 서비스가 활성화되도록 설정
public class CloudFileStorageService implements FileStorageService {

    // 클라우드 스토리지 버킷 이름 설정 (application.yml에서 주입)
    @Value("${GCP_BUCKET_NAME}")
    private String bucketName;

    // Google Cloud Storage 클라이언트 객체
    private final Storage gcsStorage;

    // 생성자를 통한 Storage 객체 주입 (Spring이 자동으로 StorageOptions.getDefaultInstance().getService()를 통해 생성)
    public CloudFileStorageService(Storage gcsStorage) {
        this.gcsStorage = gcsStorage;
    }

    @Value(("${file.upload.max-size:10485760}"))
    private long maxFileSize; // 최대 파일 크기 (기본값: 10MB)

    @Override
    public String storeFile(MultipartFile file, String subFolder) {
        // 파일 크기 제한 (예: 10MB)
        if (file.getSize() > maxFileSize) {
            log.warn("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다. 파일명: {}", file.getOriginalFilename());
            throw new RuntimeException("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        }

        try {
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedFileType(contentType)) {
                log.warn("허용되지 않는 파일 형식입니다. 파일명: {}, Content-Type: {}", file.getOriginalFilename(), contentType);
                throw new RuntimeException("허용되지 않는 파일 형식입니다.");
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = getExtension(originalFileName);
            // 클라우드 스토리지 내에서 객체 이름 (경로 포함) 구성
            // 예: "post_subfolder/UUID.extension"
            String storedObjectName = String.format("%s/%s%s", subFolder, UUID.randomUUID(), fileExtension);

            // BlobId: 버킷 이름과 객체 이름으로 구성된 객체 식별자
            BlobId blobId = BlobId.of(bucketName, storedObjectName);
            // BlobInfo: 객체의 메타데이터 (Content-Type 등)
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // GCS에 파일 업로드
            Blob blob = gcsStorage.createFrom(blobInfo, file.getInputStream());

            // 업로드된 파일의 공개 URL 반환 (공개 액세스 방지가 적용되어도 이 URL 자체는 생성됨)
            // 실제 접근은 인증된 서비스 계정을 통해서만 가능
            String fileUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, storedObjectName);
            log.info("Google Cloud Storage에 파일 저장 성공: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("파일 스트림 처리 중 오류 발생", e);
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("Google Cloud Storage 파일 저장 실패", e);
            throw new RuntimeException("클라우드 스토리지 파일 저장 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void deletePhysicalFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("삭제할 파일 URL이 비어있습니다.");
            return;
        }

        // GCS URL에서 버킷 이름과 객체 이름 추출
        // 예: https://storage.googleapis.com/your-bucket-name/your-object-name
        String gcsUrlPrefix = String.format("https://storage.googleapis.com/%s/", bucketName);
        if (!fileUrl.startsWith(gcsUrlPrefix)) {
            log.warn("유효하지 않은 Google Cloud Storage URL입니다: {}", fileUrl);
            return;
        }

        // 수정: URL 디코딩 추가: URL에 인코딩된 특수 문자나 공백이 포함된 경우를 대비
        String objectNameToDelete = URLDecoder.decode(
                fileUrl.substring(gcsUrlPrefix.length()),
                StandardCharsets.UTF_8
        );

        try {
            // BlobId를 사용하여 객체 삭제
            boolean deleted = gcsStorage.delete(BlobId.of(bucketName, objectNameToDelete));
            if (deleted) {
                log.info("Google Cloud Storage에서 파일 삭제 성공: {}", fileUrl);
            } else {
                log.warn("Google Cloud Storage에서 파일 삭제 실패 (파일을 찾을 수 없거나 권한 없음): {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("Google Cloud Storage 파일 삭제 실패", e);
            throw new RuntimeException("클라우드 스토리지 파일 삭제 중 오류가 발생했습니다.");
        }
    }

    // 파일 확장자 추출 헬퍼 메서드
    private String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex != -1 ? fileName.substring(dotIndex) : "";
    }

    // 허용하는 파일 타입 검사 헬퍼 메서드
    private boolean isAllowedFileType(String contentType) {
        return contentType.startsWith("image/") ||
                contentType.equals("application/pdf") ||
                contentType.startsWith("text/");
    }
}
