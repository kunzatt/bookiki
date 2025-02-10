package com.corp.bookiki.user.service;

import com.corp.bookiki.common.service.ImageFileService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.ProfileResponse;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    @Value("${file.storage.default-image}")
    private String defaultImageUrl;

    private final ImageFileService imageFileService;
    private final UserRepository userRepository;
    private static final String PROFILE_IMAGE_PATH = "/images/profile/";

    // 프로필 사진 변경
    @Transactional
    public void updateProfileImage(Integer userId, MultipartFile file) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        String currentImageUrl = user.getProfileImage();
        // 기존 이미지 삭제
        if(currentImageUrl != null || !currentImageUrl.equals(defaultImageUrl)) {
            imageFileService.deleteFile(currentImageUrl);
        }

        // 새 이미지 업로드
        try{
            String newImageUrl = imageFileService.uploadFile(file, PROFILE_IMAGE_PATH);
            user.updateProfileImage(newImageUrl);
        } catch (IOException ex) {
            throw new FileException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 프로필 사진 삭제
    @Transactional
    public void deleteProfileImage(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        String currentImageUrl = user.getProfileImage();
        // 기존 이미지 삭제
        if(currentImageUrl != null || !currentImageUrl.equals(defaultImageUrl)) {
            imageFileService.deleteFile(currentImageUrl);
        }
        try{
            user.updateProfileImage(defaultImageUrl);
        } catch (Exception ex) {
            throw new FileException(ErrorCode.FILE_DELETE_FAILED);
        }

    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileImage(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return new ProfileResponse(user.getProfileImage());
    }
}
