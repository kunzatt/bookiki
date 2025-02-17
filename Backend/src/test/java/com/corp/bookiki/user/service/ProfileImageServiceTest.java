package com.corp.bookiki.user.service;

import com.corp.bookiki.common.service.ImageFileService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.ProfileResponse;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ProfileImageServiceTest {

    @InjectMocks
    private ProfileImageService profileImageService;

    @Mock
    private ImageFileService imageFileService;

    @Mock
    private UserRepository userRepository;

    private final String DEFAULT_IMAGE_URL = "default-image-url";
    private final Integer TEST_USER_ID = 1;
    private final String PROFILE_IMAGE_PATH = "/images/profile/";

    @Test
    @DisplayName("프로필 이미지 업데이트 성공")
    void updateProfileImage_WhenValidRequest_ThenSuccess() throws IOException {
        // given
        log.info("프로필 이미지 업데이트 테스트 시작");
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes()
        );
        UserEntity user = UserEntity.builder()
            .profileImage(DEFAULT_IMAGE_URL)
            .build();
        String newImageUrl = PROFILE_IMAGE_PATH + "new-image.jpg";

        ReflectionTestUtils.setField(profileImageService, "defaultImageUrl", DEFAULT_IMAGE_URL);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(java.util.Optional.of(user));
        when(imageFileService.uploadFile(any(MultipartFile.class), eq("/images/profile"))).thenReturn(newImageUrl);
        log.info("Mock 설정 완료");

        // when
        profileImageService.updateProfileImage(TEST_USER_ID, file);
        log.info("프로필 이미지 업데이트 실행");

        // then
        verify(imageFileService).uploadFile(any(MultipartFile.class), eq("/images/profile"));
        assertThat(user.getProfileImage()).isEqualTo(newImageUrl);
        log.info("프로필 이미지 업데이트 테스트 완료");
    }

    @Test
    @DisplayName("이전 프로필 이미지가 있는 경우 삭제 후 업데이트 성공")
    void updateProfileImage_WhenPreviousImageExists_ThenDeleteAndUpdate() throws IOException {
        // given
        log.info("이전 프로필 이미지 삭제 후 업데이트 테스트 시작");
        String previousImageUrl = PROFILE_IMAGE_PATH + "previous-image.jpg";
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes()
        );
        UserEntity user = UserEntity.builder()
            .profileImage(previousImageUrl)
            .build();
        String newImageUrl = PROFILE_IMAGE_PATH + "new-image.jpg";

        ReflectionTestUtils.setField(profileImageService, "defaultImageUrl", DEFAULT_IMAGE_URL);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(java.util.Optional.of(user));
        when(imageFileService.uploadFile(any(MultipartFile.class), eq("/images/profile"))).thenReturn(newImageUrl);
        log.info("Mock 설정 완료");

        // when
        profileImageService.updateProfileImage(TEST_USER_ID, file);
        log.info("프로필 이미지 업데이트 실행");

        // then
        verify(imageFileService).deleteFile(previousImageUrl);
        verify(imageFileService).uploadFile(any(MultipartFile.class), eq("/images/profile"));
        assertThat(user.getProfileImage()).isEqualTo(newImageUrl);
        log.info("이전 프로필 이미지 삭제 후 업데이트 테스트 완료");
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 프로필 이미지 업데이트 시도")
    void updateProfileImage_WhenUserNotFound_ThenThrowException() {
        // given
        log.info("존재하지 않는 사용자 프로필 이미지 업데이트 테스트 시작");
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes()
        );
        when(userRepository.findById(TEST_USER_ID)).thenReturn(java.util.Optional.empty());
        log.info("Mock 설정 완료");

        // when & then
        assertThatThrownBy(() -> profileImageService.updateProfileImage(TEST_USER_ID, file))
            .isInstanceOf(UserException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        log.info("존재하지 않는 사용자 프로필 이미지 업데이트 테스트 완료");
    }

    @Test
    @DisplayName("파일 업로드 실패 시 예외 발생")
    void updateProfileImage_WhenUploadFails_ThenThrowException() throws IOException {
        // given
        log.info("파일 업로드 실패 테스트 시작");
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes()
        );
        UserEntity user = UserEntity.builder()
            .profileImage(DEFAULT_IMAGE_URL)
            .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(java.util.Optional.of(user));
        when(imageFileService.uploadFile(any(), anyString())).thenThrow(new IOException());
        log.info("Mock 설정 완료");

        // when & then
        assertThatThrownBy(() -> profileImageService.updateProfileImage(TEST_USER_ID, file))
            .isInstanceOf(FileException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_FAILED);
        log.info("파일 업로드 실패 테스트 완료");
    }

    @Test
    @DisplayName("프로필 이미지 삭제 성공")
    void deleteProfileImage_WhenValidRequest_ThenSuccess() {
        // given
        log.info("프로필 이미지 삭제 테스트 시작");
        String currentImageUrl = "/images/profile/current-image.jpg";
        UserEntity user = UserEntity.builder()
            .profileImage(currentImageUrl)
            .build();

        ReflectionTestUtils.setField(profileImageService, "defaultImageUrl", DEFAULT_IMAGE_URL);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(java.util.Optional.of(user));
        log.info("Mock 설정 완료");

        // when
        profileImageService.deleteProfileImage(TEST_USER_ID);
        log.info("프로필 이미지 삭제 실행");

        // then
        verify(imageFileService, times(1)).deleteFile(currentImageUrl);
        assertThat(user.getProfileImage()).isEqualTo(DEFAULT_IMAGE_URL);
        log.info("프로필 이미지 삭제 테스트 완료");
    }


    @Test
    @DisplayName("프로필 이미지 조회 성공")
    void getProfileImage_WhenValidRequest_ThenSuccess() {
        // given
        log.info("프로필 이미지 조회 테스트 시작");
        String imageUrl = PROFILE_IMAGE_PATH + "test-image.jpg";
        UserEntity user = UserEntity.builder()
            .profileImage(imageUrl)
            .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(java.util.Optional.of(user));
        log.info("Mock 설정 완료");

        // when
        ProfileResponse response = profileImageService.getProfileImage(TEST_USER_ID);
        log.info("프로필 이미지 조회 실행");

        // then
        assertThat(response.getProfileImageUrl()).isEqualTo(imageUrl);
        log.info("프로필 이미지 조회 테스트 완료");
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 프로필 이미지 조회 시도")
    void getProfileImage_WhenUserNotFound_ThenThrowException() {
        // given
        log.info("존재하지 않는 사용자 프로필 이미지 조회 테스트 시작");
        when(userRepository.findById(TEST_USER_ID)).thenReturn(java.util.Optional.empty());
        log.info("Mock 설정 완료");

        // when & then
        assertThatThrownBy(() -> profileImageService.getProfileImage(TEST_USER_ID))
            .isInstanceOf(UserException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        log.info("존재하지 않는 사용자 프로필 이미지 조회 테스트 완료");
    }
}
