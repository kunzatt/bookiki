package com.corp.bookiki.user.service;

import com.corp.bookiki.common.service.ImageFileService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.ProfileResponse;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

    @Value("${spring.file.storage.default-image}")
    private String defaultImageUrl;

    private final ImageFileService imageFileService;
    private final UserRepository userRepository;
    private static final String PROFILE_IMAGE_PATH = "/images/profile";

    // 프로필 사진 변경
    @Transactional
    public void updateProfileImage(Integer userId, MultipartFile file) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        String currentImageUrl = user.getProfileImage();

        try {
            // 새 이미지 먼저 업로드
            String newImageUrl = imageFileService.uploadFile(file, PROFILE_IMAGE_PATH);

            // 기존 이미지가 있고 기본 이미지가 아닌 경우에만 삭제
            if(currentImageUrl != null && !currentImageUrl.equals(defaultImageUrl)) {
                try {
                    imageFileService.deleteFile(currentImageUrl);
                } catch (FileException e) {
                    log.warn("Failed to delete old profile image: {}", currentImageUrl);
                    // 이전 이미지 삭제 실패해도 계속 진행
                }
            }

            user.updateProfileImage(newImageUrl);
        } catch (IOException ex) {
            log.error("Failed to update profile image for user {}: {}", userId, ex.getMessage());
            throw new FileException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 프로필 사진 삭제
    @Transactional
    public void deleteProfileImage(Integer userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        String currentImageUrl = user.getProfileImage();

        // 기본 이미지가 아닌 경우에만 파일 삭제
        if(currentImageUrl != null && !currentImageUrl.equals(defaultImageUrl)) {
            imageFileService.deleteFile(currentImageUrl);
        }

        user.updateProfileImage(defaultImageUrl);
    }

    @Transactional(readOnly = true)
    public byte[] getActualProfileImage(Integer userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String imageUrl = user.getProfileImage();
        try {
            return imageFileService.getFileAsByteArray(imageUrl);
        } catch (IOException ex) {
            log.error("Failed to read profile image for user {}: {}", userId, ex.getMessage());
            throw new FileException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileImage(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return new ProfileResponse(user.getProfileImage());
    }
}
