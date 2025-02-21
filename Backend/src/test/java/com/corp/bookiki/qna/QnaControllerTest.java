package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.JacksonConfig;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.qna.controller.QnaController;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.dto.QnaUpdate;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.service.QnaService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserService;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({QnaController.class})
@Import({WebMvcConfig.class, CurrentUserArgumentResolver.class, TestSecurityBeansConfig.class, SecurityConfig.class, CookieUtil.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("문의사항 컨트롤러 테스트")
@Slf4j
class QnaControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @MockBean
    private QnaService qnaService;

    @MockBean
    private UserService userService;

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
    @WithMockUser
    @DisplayName("문의사항 등록 성공")
    void createQna_Success() throws Exception {
        // given
        QnaRequest request = new QnaRequest("테스트 제목", "일반문의", "테스트 내용");
        given(qnaService.createQna(any(QnaRequest.class), eq(2))).willReturn(1);

        // when
        ResultActions result = mockMvc.perform(post("/api/qna")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .cookie(getUserJwtCookie()));

        // then
        result.andExpect(status().isCreated())
            .andExpect(content().string("1"))
            .andDo(print());
        verify(qnaService).createQna(any(QnaRequest.class), eq(2));
    }

    @Test
    @WithMockUser
    @DisplayName("문의사항 수정 성공")
    void updateQna_Success() throws Exception {
        // given
        QnaUpdate update = new QnaUpdate(1, "수정된 제목", "일반문의", "수정된 내용");
        doNothing().when(qnaService).updateQna(any(QnaUpdate.class), eq(2));

        // when
        ResultActions result = mockMvc.perform(put("/api/qna")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(update))
            .cookie(getUserJwtCookie()));

        // then
        result.andExpect(status().isOk())
            .andDo(print());
        verify(qnaService).updateQna(any(QnaUpdate.class), eq(2));
    }

    @Test
    @WithMockUser
    @DisplayName("문의사항 삭제 성공")
    void deleteQna_Success() throws Exception {
        // given
        int qnaId = 1;
        doNothing().when(qnaService).deleteQna(eq(qnaId), eq(2));

        // when
        ResultActions result = mockMvc.perform(delete("/api/qna/{id}", qnaId)
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(getUserJwtCookie()));

        // then
        result.andExpect(status().isNoContent())
            .andDo(print());
        verify(qnaService).deleteQna(eq(qnaId), eq(2));
    }

    @Test
    @WithMockUser
    @DisplayName("문의사항 목록 조회 성공")
    void selectQnas_Success() throws Exception {
        // given
        UserEntity testUser = UserEntity.builder()
            .userName("Test User")
            .email("test@example.com")
            .role(Role.USER)
            .build();
        ReflectionTestUtils.setField(testUser, "id", 1);

        QnaEntity qna = QnaEntity.builder()
            .title("테스트 제목")
            .qnaType("일반문의")
            .content("테스트 내용")
            .user(testUser)
            .build();
        ReflectionTestUtils.setField(qna, "id", 1);

        List<QnaEntity> qnaList = List.of(qna);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<QnaEntity> qnaPage = new PageImpl<>(qnaList, pageRequest, qnaList.size());

        given(qnaService.selectQnas(
            isNull(),
            isNull(),
            isNull(),
            any(AuthUser.class),
            any(Pageable.class)
        )).willReturn(qnaPage);
        given(userService.getUserById(anyInt())).willReturn(testUser);

        // when
        ResultActions result = mockMvc.perform(get("/api/qna")
            .contentType(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "10")
            .cookie(getUserJwtCookie()));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content[0].title").value("테스트 제목"))
            .andExpect(jsonPath("$.content[0].authorName").value("Test User"))
            .andExpect(jsonPath("$.pageable").exists())
            .andExpect(jsonPath("$.totalElements").exists())
            .andDo(print());
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