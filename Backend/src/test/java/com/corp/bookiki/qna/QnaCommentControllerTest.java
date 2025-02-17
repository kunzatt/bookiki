package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.qna.controller.QnaCommentController;
import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.service.QnaCommentService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QnaCommentController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
    CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("문의사항 답변 컨트롤러 테스트")
@Slf4j
class QnaCommentControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QnaCommentService qnaCommentService;

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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("문의사항 답변 등록 성공")
    void createQnaComment_Success() throws Exception {
        // given
        QnaCommentRequest request = new QnaCommentRequest(1, "테스트 답변 내용");

        given(qnaCommentService.createQnaComment(any(QnaCommentRequest.class), eq(1)))
            .willReturn(1);

        // when
        ResultActions result = mockMvc.perform(post("/api/admin/qna")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .cookie(getAdminJwtCookie()));

        // then
        result.andExpect(status().isCreated())
            .andExpect(content().string("1"))
            .andDo(print());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("권한이 없는 사용자의 문의사항 답변 등록 실패")
    void createQnaComment_WithoutAdminRole_Fail() throws Exception {
        // given
        QnaCommentRequest request = new QnaCommentRequest(1,"테스트 답변 내용");

        // when
        ResultActions result = mockMvc.perform(post("/api/admin/qna")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .cookie(getUserJwtCookie()));

        // then
        result.andExpect(status().isForbidden())
            .andDo(print());
        verify(qnaCommentService, never()).createQnaComment(any(QnaCommentRequest.class), anyInt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("문의사항 답변 수정 성공")
    void updateQnaComment_Success() throws Exception {
        // given
        QnaCommentUpdate update = new QnaCommentUpdate(1, "수정된 답변 내용");
        doNothing().when(qnaCommentService).updateQnaComment(any(QnaCommentUpdate.class));

        // when
        ResultActions result = mockMvc.perform(put("/api/admin/qna")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(update))
            .cookie(getAdminJwtCookie()));

        // then
        result.andExpect(status().isOk())
            .andDo(print());
        verify(qnaCommentService).updateQnaComment(any(QnaCommentUpdate.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("문의사항 답변 삭제 성공")
    void deleteQnaComment_Success() throws Exception {
        // given
        int commentId = 1;
        doNothing().when(qnaCommentService).deleteQnaComment(commentId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/admin/qna/{id}", commentId)
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(getAdminJwtCookie()));

        // then
        result.andExpect(status().isNoContent())
            .andDo(print());
        verify(qnaCommentService).deleteQnaComment(commentId);
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