package com.corp.bookiki.common.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageFileService {
    @Value("${spring.file.storage.path}")
    private String storagePath;

    @Value("${spring.file.storage.default-image}")
    private String defaultImagePath;

    public String uploadFile(MultipartFile file, String directoryPath) throws IOException {
        validateFile(file);
        String savedPath = storagePath + directoryPath;  // ex) /storage/profile/, /storage/board/ 등

        String savedFileName = createFileName(file.getOriginalFilename());
        try {
            File directory = new File(savedPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File dest = new File(savedPath + savedFileName);
            file.transferTo(dest);

            return directoryPath + savedFileName;  // ex) /images/profile/xxx.jpg
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new FileException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 파일 삭제
    public void deleteFile(String fileUrl) {
        try {
            String filePath = storagePath + fileUrl;
            log.info("Attempting to delete file at: {}", filePath);

            File file = new File(filePath);

            if (!file.exists()) {
                log.warn("File does not exist at path: {}", filePath);
                throw new FileException(ErrorCode.FILE_NOT_FOUND);
            }

            if (!file.delete()) {
                log.error("Failed to delete file at path: {}", filePath);
                throw new FileException(ErrorCode.FILE_DELETE_FAILED);
            }

            log.info("Successfully deleted file at: {}", filePath);
        } catch (SecurityException ex) {
            log.error("Security exception while deleting file: {}", ex.getMessage(), ex);
            throw new FileException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    public byte[] getFileAsByteArray(String fileUrl) throws IOException {
        if (fileUrl == null) {
            log.info("fileUrl is null, trying to get default image");
            return getDefaultImage();
        }

        try {
            String filePath = storagePath + fileUrl;
            log.info("Trying to read file from: {}", filePath);

            Path path = Paths.get(filePath);
            log.info("Absolute path: {}", path.toAbsolutePath());
            log.info("File exists: {}", Files.exists(path));

            if (!Files.exists(path)) {
                log.warn("File not found at: {}. Returning default image", filePath);
                return getDefaultImage();
            }

            return Files.readAllBytes(path);
        } catch (Exception e) {
            log.error("Error reading file: {}", e.getMessage(), e);
            return getDefaultImage();
        }
    }

    private byte[] getDefaultImage() throws IOException {
        String defaultImageFullPath = defaultImagePath;
        log.info("Trying to load default image from: {}", defaultImageFullPath);

        Path defaultPath = Paths.get(defaultImageFullPath);
        log.info("Default image absolute path: {}", defaultPath.toAbsolutePath());
        log.info("Default image exists: {}", Files.exists(defaultPath));

        if (!Files.exists(defaultPath)) {
            log.error("Default image not found at: {}", defaultPath);
            throw new FileException(ErrorCode.FILE_NOT_FOUND);
        }

        return Files.readAllBytes(defaultPath);
    }

    private String getActualFilePath(String fileUrl) {
        return storagePath + fileUrl;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileException(ErrorCode.FILE_NOT_FOUND);
        }

        long maxSize = 10 * 1024 * 1024; // 10MB in bytes
        if (file.getSize() > maxSize) {
            throw new FileException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileException(ErrorCode.FILE_INVALID_FORMAT);
        }

        // 허용된 이미지 형식 검증
        List<String> allowedTypes = Arrays.asList(
                "image/jpeg",
                "image/png"
        );

        if (!allowedTypes.contains(contentType)) {
            throw new FileException(ErrorCode.FILE_INVALID_FORMAT);
        }

    }

    private String createFileName(String originalFilename) {
        return UUID.randomUUID().toString() + getFileExtension(originalFilename);
    }

    private String getFileExtension(String filename) {
        try {
            return filename.substring(filename.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new FileException(ErrorCode.FILE_INVALID_FORMAT);
        }
    }

    // 테스트를 위한 메서드
    public File getFile(String path) {
        return new File(path);
    }
}
