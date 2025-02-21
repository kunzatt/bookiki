package com.corp.bookiki.notice;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.notice.controller.NoticeController;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeUpdate;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.service.NoticeService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@WebMvcTest(NoticeController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
    CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("공지사항 컨트롤러 테스트")
class NoticeControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeService noticeService;  // NoticeService를 Mock으로 설정

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private UserEntity testUserEntity;
    private UserEntity testAdminEntity;
    private String testUserEmail;
    private String testAdminEmail;

    // 테스트 실행 전 설정
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
    @DisplayName("공지사항 작성 성공")
    void createNotice() throws Exception {
        // given
        NoticeRequest request = new NoticeRequest("제목", "내용");
        given(noticeService.createNotice(any())).willReturn(1);

        // when
        ResultActions result = mockMvc.perform(post("/api/admin/notices")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .cookie(getAdminJwtCookie()));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$").value(1))
            .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("공지사항 목록 조회 및 검색 테스트")
    void selectAllNotices() throws Exception {
        // given
        NoticeEntity notice = NoticeEntity.builder()
            .title("Important Notice")
            .content("Test Content")
            .build();
        ReflectionTestUtils.setField(notice, "id", 1);
        List<NoticeEntity> notices = List.of(notice);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NoticeEntity> noticePage = new PageImpl<>(notices, pageRequest, notices.size());
        given(noticeService.selectAllNotices(any(Pageable.class))).willReturn(noticePage);

        // when
        ResultActions result = mockMvc.perform(get("/api/notices")
            .contentType(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "10")
            .param("sort", "createdAt,desc")
            .cookie(getUserJwtCookie()));

        // then
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Important Notice"))
            .andDo(print());
    }

    @Test
    @DisplayName("공지사항 1개 조회 성공")
    @WithMockUser
    void selectNoticeById() throws Exception {
        // given
        int noticeId = 1;
        NoticeEntity noticeEntity = NoticeEntity.builder()
            .title("title")
            .content("content")
            .build();
        given(noticeService.selectNoticeById(noticeId)).willReturn(noticeEntity);

        // when
        ResultActions result = mockMvc.perform(get("/api/notices/{id}", noticeId)
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(getUserJwtCookie()));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("title"))
            .andExpect(jsonPath("$.content").value("content"))
            .andDo(print());
    }

    @Test
    @DisplayName("공지사항 수정 성공")
    @WithMockUser(roles = "ADMIN")
    void updateNotice() throws Exception {
        // given
        NoticeUpdate update = NoticeUpdate.builder()
            .id(1)
            .title("newTitle")
            .content("newContent")
            .build();
        doNothing().when(noticeService).updateNotice(any(NoticeUpdate.class));

        // when
        ResultActions result = mockMvc.perform(put("/api/admin/notices")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(update))
            .cookie(getAdminJwtCookie()));

        // then
        result.andExpect(status().isOk())
            .andDo(print());
        verify(noticeService).updateNotice(any(NoticeUpdate.class));
    }

    @Test
    @DisplayName("공지사항 삭제 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteNotice() throws Exception {
        // given
        int noticeId = 1;
        doNothing().when(noticeService).deleteNotice(noticeId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/admin/notices/{id}", noticeId)
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(getAdminJwtCookie()));

        // then
        result.andExpect(status().isNoContent())
            .andDo(print());
        verify(noticeService).deleteNotice(noticeId);
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