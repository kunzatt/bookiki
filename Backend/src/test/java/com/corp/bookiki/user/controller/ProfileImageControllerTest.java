package com.corp.bookiki.user.controller;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import com.corp.bookiki.user.dto.ProfileResponse;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.ProfileImageService;
import com.corp.bookiki.util.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(ProfileImageController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class ProfileImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileImageService profileImageService;

    @Autowired
    private UserRepository userRepository;

    private UserEntity mockUserEntity;

    @BeforeEach
    void setUp() {
        // 테스트용 UserEntity 생성
        mockUserEntity = UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .userName("테스트")
                .companyId("CORP001")
                .role(Role.USER)
                .provider(Provider.BOOKIKI)
                .build();

        // 리플렉션을 사용하여 id 설정
        try {
            Field idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockUserEntity, 1);
        } catch (Exception e) {
            log.error("Failed to set id field", e);
        }

        // UserRepository 모킹
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(mockUserEntity));

        // SecurityContext에 Authentication 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 API 성공")
    @WithMockUser(username = "test@example.com")
    void updateProfileImage_WhenValidRequest_ThenSuccess() throws Exception {
        log.info("프로필 이미지 업데이트 API 테스트 시작");

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
        log.info("테스트용 MultipartFile 생성 완료");

        doNothing().when(profileImageService).updateProfileImage(anyInt(), any(MultipartFile.class));
        log.info("Mock 서비스 동작 설정 완료");

        mockMvc.perform(multipart("/api/users/1/profile-image")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        verify(profileImageService, times(1)).updateProfileImage(anyInt(), any(MultipartFile.class));
        log.info("프로필 이미지 업데이트 API 테스트 완료");
    }

    @Test
    @DisplayName("프로필 이미지 삭제 API 성공")
    @WithMockUser
    void deleteProfileImage_WhenValidRequest_ThenSuccess() throws Exception {
        log.info("프로필 이미지 삭제 API 테스트 시작");

        doNothing().when(profileImageService).deleteProfileImage(anyInt());
        log.info("Mock 서비스 동작 설정 완료");

        mockMvc.perform(delete("/api/users/1/profile-image")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(profileImageService, times(1)).deleteProfileImage(anyInt());
        log.info("프로필 이미지 삭제 API 테스트 완료");
    }

    @Test
    @DisplayName("프로필 이미지 조회 API 성공")
    @WithMockUser
    void getProfileImage_WhenValidRequest_ThenSuccess() throws Exception {
        log.info("프로필 이미지 조회 API 테스트 시작");

        ProfileResponse response = ProfileResponse.builder()
                .profileImageUrl("/images/profile/test.jpg")
                .build();
        log.info("테스트용 응답 객체 생성 완료: {}", response);

        when(profileImageService.getProfileImage(anyInt())).thenReturn(response);
        log.info("Mock 서비스 동작 설정 완료");

        mockMvc.perform(get("/api/users/1/profile-image")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl").value("/images/profile/test.jpg"));

        verify(profileImageService, times(1)).getProfileImage(anyInt());
        log.info("프로필 이미지 조회 API 테스트 완료");
    }

    @Test
    @DisplayName("잘못된 파일 형식으로 프로필 이미지 업데이트 시 실패")
    @WithMockUser
    void updateProfileImage_WhenInvalidFormat_ThenFailure() throws Exception {
        log.info("잘못된 파일 형식 프로필 이미지 업데이트 API 테스트 시작");

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test file content".getBytes()
        );
        log.info("테스트용 잘못된 형식의 MultipartFile 생성 완료");

        doThrow(new FileException(ErrorCode.FILE_INVALID_FORMAT))
                .when(profileImageService).updateProfileImage(anyInt(), any(MultipartFile.class));
        log.info("Mock 서비스 예외 발생 설정 완료");

        mockMvc.perform(multipart("/api/users/1/profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");  // PUT 메서드로 명시적 설정
                            return request;
                        }))
                .andExpect(status().isBadRequest());

        verify(profileImageService, times(1)).updateProfileImage(anyInt(), any(MultipartFile.class));
        log.info("잘못된 파일 형식 프로필 이미지 업데이트 API 테스트 완료");
    }
}