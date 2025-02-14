package com.corp.bookiki.chatbot;

import com.corp.bookiki.chatbot.controller.ChatbotController;
import com.corp.bookiki.chatbot.service.ChatbotService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.Date;
import java.util.Optional;

@WebMvcTest({ChatbotController.class})
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
    CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("챗봇 컨트롤러 테스트")
@Slf4j
class ChatbotControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @MockBean
    private ChatbotService chatbotService;

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


}
