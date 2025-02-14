package com.corp.bookiki.user.controller;

import com.corp.bookiki.bookhistory.controller.BookHistoryController;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.FileException;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.dto.ProfileResponse;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.ProfileImageService;
import com.corp.bookiki.util.CookieUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(BookHistoryController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
    CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
class ProfileImageControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileImageService profileImageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private UserEntity testUserEntity;
    private UserEntity testAdminEntity;
    private String testUserEmail;
    private String testAdminEmail;

    @BeforeEach
    void setup() {
        // 일반 사용자 설정
        testUserEmail = "user@example.com";
        testUserEntity = UserEntity.builder()
            .email(testUserEmail)
            .userName("Test User")
            .role(Role.USER)
            .build();
        ReflectionTestUtils.setField(testUserEntity, "id", 2);

        // 관리자 설정
        testAdminEmail = "admin@example.com";
        testAdminEntity = UserEntity.builder()
            .email(testAdminEmail)
            .userName("Admin User")
            .role(Role.ADMIN)
            .build();
        ReflectionTestUtils.setField(testAdminEntity, "id", 1);

        // 일반 사용자 Claims 설정
        Claims userClaims = mock(Claims.class);
        given(userClaims.getSubject()).willReturn("user:" + testUserEmail);
        given(userClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 3600000));
        given(userClaims.get("authorities", String.class)).willReturn("ROLE_USER");

        // 관리자 Claims 설정
        Claims adminClaims = mock(Claims.class);
        given(adminClaims.getSubject()).willReturn("user:" + testAdminEmail);
        given(adminClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 3600000));
        given(adminClaims.get("authorities", String.class)).willReturn("ROLE_USER,ROLE_ADMIN");

        // JWT Service 모킹
        given(jwtService.validateToken(eq("user-jwt-token"))).willReturn(true);
        given(jwtService.validateToken(eq("admin-jwt-token"))).willReturn(true);
        given(jwtService.extractAllClaims(eq("user-jwt-token"))).willReturn(userClaims);
        given(jwtService.extractAllClaims(eq("admin-jwt-token"))).willReturn(adminClaims);
        given(jwtService.extractEmail(eq("user-jwt-token"))).willReturn(testUserEmail);
        given(jwtService.extractEmail(eq("admin-jwt-token"))).willReturn(testAdminEmail);
        given(jwtService.isTokenExpired(anyString())).willReturn(false);

        // UserRepository 모킹
        given(userRepository.findByEmail(eq(testUserEmail)))
            .willReturn(Optional.of(testUserEntity));
        given(userRepository.findByEmail(eq(testAdminEmail)))
            .willReturn(Optional.of(testAdminEntity));

        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
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
                .cookie(getUserJwtCookie())
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
                .cookie(getUserJwtCookie())
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
                .cookie(getUserJwtCookie())
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
                .cookie(getUserJwtCookie())
                        .with(request -> {
                            request.setMethod("PUT");  // PUT 메서드로 명시적 설정
                            return request;
                        }))
                .andExpect(status().isBadRequest());

        verify(profileImageService, times(1)).updateProfileImage(anyInt(), any(MultipartFile.class));
        log.info("잘못된 파일 형식 프로필 이미지 업데이트 API 테스트 완료");
    }

    private Cookie getUserJwtCookie() {
        Cookie accessTokenCookie = new Cookie("access_token", "user-jwt-token");
        accessTokenCookie.setPath("/");
        return accessTokenCookie;
    }

    private Cookie getAdminJwtCookie() {
        Cookie accessTokenCookie = new Cookie("access_token", "admin-jwt-token");
        accessTokenCookie.setPath("/");
        return accessTokenCookie;
    }
}