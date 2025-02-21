package com.corp.bookiki.shelf;

import com.corp.bookiki.bookinformation.entity.Category;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.ShelfException;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.shelf.controller.ShelfController;
import com.corp.bookiki.shelf.dto.ShelfCreateRequest;
import com.corp.bookiki.shelf.dto.ShelfResponse;
import com.corp.bookiki.shelf.dto.ShelfUpdateRequest;
import com.corp.bookiki.shelf.service.ShelfService;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@WebMvcTest(ShelfController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
    CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("선반 컨트롤러 테스트")
@Slf4j
class ShelfControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShelfService shelfService;

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
    @DisplayName("전체 책장 조회 성공")
    void selectAllShelf_Success() throws Exception {
        // given
        ShelfResponse shelf1 = new ShelfResponse();
        shelf1.setId(1);
        shelf1.setShelfNumber(1);
        shelf1.setLineNumber(2);
        shelf1.setCategory(Category.COMPUTER_SCIENCE);

        ShelfResponse shelf2 = new ShelfResponse();
        shelf2.setId(2);
        shelf2.setShelfNumber(2);
        shelf2.setLineNumber(3);
        shelf2.setCategory(Category.PHILOSOPHY_PSYCHOLOGY);

        List<ShelfResponse> responses = Arrays.asList(shelf1, shelf2);

        when(shelfService.selectAllShelf()).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                .cookie(getAdminJwtCookie()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].shelfNumber").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].shelfNumber").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("책장 생성 성공")
    void createShelf_Success() throws Exception {
        // given
        ShelfCreateRequest request = new ShelfCreateRequest();
        request.setShelfNumber(1);
        request.setLineNumber(2);
        request.setCategory(Category.COMPUTER_SCIENCE);

        when(shelfService.createShelf(any(ShelfCreateRequest.class))).thenReturn(1);

        // when & then
        mockMvc.perform(post("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                .cookie(getAdminJwtCookie())
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("잘못된 책장 번호로 생성 실패")
    void createShelf_WithInvalidShelfNumber_ReturnsBadRequest() throws Exception {
        // given
        ShelfCreateRequest request = new ShelfCreateRequest();
        request.setShelfNumber(0);
        request.setLineNumber(2);
        request.setCategory(Category.COMPUTER_SCIENCE);

        when(shelfService.createShelf(any(ShelfCreateRequest.class)))
                .thenThrow(new ShelfException(ErrorCode.INVALID_SHELF_NUMBER));

        // when & then
        mockMvc.perform(post("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                .cookie(getAdminJwtCookie())
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 책장 번호입니다"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("책장 수정 성공")
    void updateShelf_Success() throws Exception {
        // given
        ShelfUpdateRequest request = new ShelfUpdateRequest();
        request.setId(1);
        request.setShelfNumber(2);
        request.setLineNumber(3);
        request.setCategory(Category.PHILOSOPHY_PSYCHOLOGY);

        doNothing().when(shelfService).updateShelf(any(ShelfUpdateRequest.class));

        // when & then
        mockMvc.perform(put("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                .cookie(getAdminJwtCookie())
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("존재하지 않는 책장 수정 시도시 404 반환")
    void updateShelf_WithNonExistentId_ReturnsNotFound() throws Exception {
        // given
        ShelfUpdateRequest request = new ShelfUpdateRequest();
        request.setId(999);
        request.setShelfNumber(2);
        request.setLineNumber(3);
        request.setCategory(Category.PHILOSOPHY_PSYCHOLOGY);

        doThrow(new ShelfException(ErrorCode.SHELF_NOT_FOUND))
                .when(shelfService).updateShelf(any(ShelfUpdateRequest.class));

        // when & then
        mockMvc.perform(put("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                .cookie(getAdminJwtCookie())
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("책장을 찾을 수 없습니다"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("책장 삭제 성공")
    void deleteShelf_Success() throws Exception {
        // given
        int id = 1;
        doNothing().when(shelfService).deleteShelf(id);

        // when & then
        mockMvc.perform(delete("/api/admin/shelf/categories/{id}", id)  // URL 패턴 수정
                        .contentType(MediaType.APPLICATION_JSON)
                .cookie(getAdminJwtCookie()))
                .andDo(print())  // 실패 시 결과를 상세히 출력
                .andExpect(status().isNoContent());

        verify(shelfService).deleteShelf(id);  // service 메서드 호출 검증
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("존재하지 않는 책장 삭제 시도시 404 반환")
    void deleteShelf_WithNonExistentId_ReturnsNotFound() throws Exception {
        // given
        int id = 999;
        doThrow(new ShelfException(ErrorCode.SHELF_NOT_FOUND))
                .when(shelfService).deleteShelf(id);

        // when & then
        mockMvc.perform(delete("/api/admin/shelf/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                .cookie(getAdminJwtCookie()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("책장을 찾을 수 없습니다"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("책장 삭제 시 서버 오류 발생")
    void deleteShelf_WhenServerError_Returns500() throws Exception {
        // given
        int id = 1;
        doThrow(new ShelfException(ErrorCode.INTERNAL_SERVER_ERROR))
                .when(shelfService).deleteShelf(id);

        // when & then
        mockMvc.perform(delete("/api/admin/shelf/categories/{id}", id)
                .cookie(getAdminJwtCookie())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다"));
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