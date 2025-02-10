package com.corp.bookiki.common.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageFileService {
    @Value("${file.storage.path}")
    private String storagePath;

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
            File file = new File(filePath);

            if (!file.exists()) {
                throw new FileException(ErrorCode.FILE_NOT_FOUND);
            }

            if (!file.delete()) {
                throw new FileException(ErrorCode.FILE_DELETE_FAILED);
            }
        } catch (SecurityException ex) {
            log.error("File delete failed", ex);
            throw new FileException(ErrorCode.FILE_DELETE_FAILED);
        }
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
    protected File getFile(String path) {
        return new File(path);
    }
}
