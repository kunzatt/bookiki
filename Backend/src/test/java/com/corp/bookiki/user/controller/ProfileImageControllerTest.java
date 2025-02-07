package com.corp.bookiki.user.controller;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.dto.ProfileResponse;
import com.corp.bookiki.user.service.ProfileImageService;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(ProfileImageController.class)
@AutoConfigureMockMvc
@DisplayName("프로필 이미지 컨트롤러 테스트")
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
class ProfileImageControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private ProfileImageService profileImageService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthUser mockUser;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        mockUser = AuthUser.builder()
                .id(1)
                .email("test@example.com")
                .build();

        log.info("MockMvc 및 MockUser 설정이 완료되었습니다.");
    }

    @Nested
    @DisplayName("프로필 이미지 업데이트 테스트")
    class UpdateProfileImage {
        @Test
        @WithMockUser
        @DisplayName("정상적인 프로필 이미지 업데이트 시 성공")
        void updateProfileImage_WhenValidRequest_ThenReturnsOk() throws Exception {
            // given
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "test-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes(StandardCharsets.UTF_8)
            );

            doNothing().when(profileImageService).updateProfileImage(anyInt(), any(MultipartFile.class));
            log.info("Mock 서비스 설정 완료: 프로필 이미지 업데이트");

            // when & then
            mockMvc.perform(multipart("/api/users/{userId}/profile-image", mockUser.getId())
                            .file(mockFile)
                            .with(csrf())
                            .requestAttr("currentUser", mockUser))
                    .andExpect(status().isOk());

            log.info("프로필 이미지 업데이트 테스트 성공");
        }

        @Test
        @WithMockUser
        @DisplayName("파일 형식이 잘못된 경우 에러 반환")
        void updateProfileImage_WhenInvalidFormat_ThenReturnsBadRequest() throws Exception {
            // given
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "test-file.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "test file content".getBytes(StandardCharsets.UTF_8)
            );

            doThrow(new FileException(ErrorCode.FILE_INVALID_FORMAT))
                    .when(profileImageService).updateProfileImage(anyInt(), any(MultipartFile.class));
            log.info("Mock 서비스 설정 완료: 잘못된 파일 형식");

            // when & then
            mockMvc.perform(multipart("/api/users/{userId}/profile-image", mockUser.getId())
                            .file(mockFile)
                            .with(csrf())
                            .requestAttr("currentUser", mockUser))
                    .andExpect(status().isBadRequest());

            log.info("잘못된 파일 형식 테스트 성공");
        }

        @Test
        @WithMockUser
        @DisplayName("사용자를 찾을 수 없는 경우 에러 반환")
        void updateProfileImage_WhenUserNotFound_ThenReturnsNotFound() throws Exception {
            // given
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "test-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes(StandardCharsets.UTF_8)
            );

            doThrow(new UserException(ErrorCode.USER_NOT_FOUND))
                    .when(profileImageService).updateProfileImage(anyInt(), any(MultipartFile.class));
            log.info("Mock 서비스 설정 완료: 사용자 없음");

            // when & then
            mockMvc.perform(multipart("/api/users/{userId}/profile-image", mockUser.getId())
                            .file(mockFile)
                            .with(csrf())
                            .requestAttr("currentUser", mockUser))
                    .andExpect(status().isNotFound());

            log.info("사용자 없음 테스트 성공");
        }
    }

    @Nested
    @DisplayName("프로필 이미지 삭제 테스트")
    class DeleteProfileImage {
        @Test
        @WithMockUser
        @DisplayName("정상적인 프로필 이미지 삭제 시 성공")
        void deleteProfileImage_WhenValidRequest_ThenReturnsNoContent() throws Exception {
            // given
            doNothing().when(profileImageService).deleteProfileImage(anyInt());
            log.info("Mock 서비스 설정 완료: 프로필 이미지 삭제");

            // when & then
            mockMvc.perform(delete("/api/users/{userId}/profile-image", mockUser.getId())
                            .with(csrf())
                            .requestAttr("currentUser", mockUser))
                    .andExpect(status().isNoContent());

            log.info("프로필 이미지 삭제 테스트 성공");
        }

        @Test
        @WithMockUser
        @DisplayName("사용자를 찾을 수 없는 경우 에러 반환")
        void deleteProfileImage_WhenUserNotFound_ThenReturnsNotFound() throws Exception {
            // given
            doThrow(new UserException(ErrorCode.USER_NOT_FOUND))
                    .when(profileImageService).deleteProfileImage(anyInt());
            log.info("Mock 서비스 설정 완료: 사용자 없음");

            // when & then
            mockMvc.perform(delete("/api/users/{userId}/profile-image", mockUser.getId())
                            .with(csrf())
                            .requestAttr("currentUser", mockUser))
                    .andExpect(status().isNotFound());

            log.info("사용자 없음 테스트 성공");
        }
    }

    @Nested
    @DisplayName("프로필 이미지 조회 테스트")
    class GetProfileImage {
        @Test
        @WithMockUser
        @DisplayName("정상적인 프로필 이미지 조회 시 성공")
        void getProfileImage_WhenValidRequest_ThenReturnsOk() throws Exception {
            // given
            ProfileResponse mockResponse = ProfileResponse.builder()
                    .profileImageUrl("/images/profile/test.jpg")
                    .build();

            given(profileImageService.getProfileImage(anyInt())).willReturn(mockResponse);
            log.info("Mock 서비스 설정 완료: 프로필 이미지 조회");

            // when & then
            mockMvc.perform(get("/api/users/{userId}/profile-image", mockUser.getId())
                            .with(csrf())
                            .requestAttr("currentUser", mockUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.profileImageUrl").value("/images/profile/test.jpg"));

            log.info("프로필 이미지 조회 테스트 성공");
        }

        @Test
        @WithMockUser
        @DisplayName("사용자를 찾을 수 없는 경우 에러 반환")
        void getProfileImage_WhenUserNotFound_ThenReturnsNotFound() throws Exception {
            // given
            given(profileImageService.getProfileImage(anyInt()))
                    .willThrow(new UserException(ErrorCode.USER_NOT_FOUND));
            log.info("Mock 서비스 설정 완료: 사용자 없음");

            // when & then
            mockMvc.perform(get("/api/users/{userId}/profile-image", mockUser.getId())
                            .with(csrf())
                            .requestAttr("currentUser", mockUser))
                    .andExpect(status().isNotFound());

            log.info("사용자 없음 테스트 성공");
        }
    }
}