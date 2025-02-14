package com.corp.bookiki.favorite;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.favorite.controller.BookFavoriteController;
import com.corp.bookiki.favorite.dto.BookFavoriteResponse;
import com.corp.bookiki.favorite.service.BookFavoriteService;
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
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(BookFavoriteController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
	CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("도서 좋아요 컨트롤러 테스트")
class BookFavoriteControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private BookFavoriteService bookFavoriteService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

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

	@Nested
	@DisplayName("도서 좋아요 목록 조회 API 테스트")
	class GetFavorites {
		@Test
		@WithMockUser
		@DisplayName("좋아요 목록 정상 조회")
		void getFavorites_Success() throws Exception {
			// given
			BookFavoriteResponse response = BookFavoriteResponse.builder()
				.id(1)
				.bookItemId(100)
				.userId(2)
				.bookTitle("테스트 도서")
				.bookImage("test-image-url")
				.createdAt(LocalDateTime.now())
				.build();

			Page<BookFavoriteResponse> page = new PageImpl<>(
				List.of(response),
				PageRequest.of(0, 20),
				1
			);

			given(bookFavoriteService.getUserFavorites(eq(2), any()))
				.willReturn(page);
			log.info("좋아요 목록 조회 테스트 시작");

			// when & then
			mockMvc.perform(get("/api/favorites")
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").exists())
				.andExpect(jsonPath("$.content[0].bookItemId").value(100))
				.andExpect(jsonPath("$.content[0].bookTitle").value("테스트 도서"))
				.andExpect(jsonPath("$.content[0].bookImage").exists())
				.andExpect(jsonPath("$.content[0].createdAt").exists());

			log.info("좋아요 목록 조회 테스트 완료");
		}

		@Test
		@DisplayName("인증되지 않은 사용자의 조회 시도")
		void getFavorites_Unauthorized() throws Exception {
			mockMvc.perform(get("/api/favorites"))
				.andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("도서 좋아요 여부 확인 API 테스트")
	class CheckFavorite {
		@Test
		@WithMockUser
		@DisplayName("좋아요 여부 확인 성공")
		void checkFavorite_Success() throws Exception {
			given(bookFavoriteService.checkFavorite(eq(2), eq(100)))
				.willReturn(true);
			log.info("좋아요 여부 확인 테스트 시작");

			mockMvc.perform(get("/api/favorites/{bookItemId}", 100)
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));

			log.info("좋아요 여부 확인 테스트 완료");
		}

		@Test
		@DisplayName("인증되지 않은 사용자의 확인 시도")
		void checkFavorite_Unauthorized() throws Exception {
			mockMvc.perform(get("/api/favorites/100"))
				.andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("도서 좋아요 토글 API 테스트")
	class ToggleFavorite {
		@Test
		@WithMockUser
		@DisplayName("좋아요 토글 성공")
		void toggleFavorite_Success() throws Exception {
			// given
			given(bookFavoriteService.toggleFavorite(eq(2), eq(100)))
				.willReturn("좋아요 추가 성공");
			log.info("좋아요 토글 테스트 시작");

			// when & then
			mockMvc.perform(post("/api/favorites/{bookItemId}", 100)
					.with(csrf())
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk())
				.andExpect(content().string("좋아요 추가 성공 성공"));

			log.info("좋아요 토글 테스트 완료");
		}

		@Test
		@WithMockUser
		@DisplayName("좋아요 취소 성공")
		void toggleFavorite_DeleteSuccess() throws Exception {
			// given
			given(bookFavoriteService.toggleFavorite(eq(2), eq(100)))
				.willReturn("좋아요 삭제");
			log.info("좋아요 취소 테스트 시작");

			// when & then
			mockMvc.perform(post("/api/favorites/{bookItemId}", 100)
					.with(csrf())
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk())
				.andExpect(content().string("좋아요 삭제 성공"));

			log.info("좋아요 취소 테스트 완료");
		}

		@Test
		@DisplayName("인증되지 않은 사용자의 토글 시도")
		void toggleFavorite_Unauthorized() throws Exception {
			mockMvc.perform(post("/api/favorites/100"))
				.andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("도서의 좋아요 수 조회 API 테스트")
	class GetBookFavoriteCount {
		@Test
		@WithMockUser
		@DisplayName("도서의 좋아요 수 조회 성공")
		void getBookFavoriteCount_Success() throws Exception {
			given(bookFavoriteService.getBookFavoriteCount(eq(100)))
				.willReturn(5);
			log.info("도서 좋아요 수 조회 테스트 시작");

			mockMvc.perform(get("/api/favorites/count/{bookItemId}", 100)
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(5));

			log.info("도서 좋아요 수 조회 테스트 완료");
		}
	}

	@Nested
	@DisplayName("내 좋아요 수 조회 API 테스트")
	class GetMyFavoriteCount {
		@Test
		@DisplayName("내 좋아요 수 조회 성공")
		void getUserFavoriteCount_Success() throws Exception {
			given(bookFavoriteService.getUserFavoriteCount(eq(2)))
				.willReturn(3);

			mockMvc.perform(get("/api/favorites/count")
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(3));
		}

		@Test
		@DisplayName("인증되지 않은 사용자의 조회 시도")
		void getUserFavoriteCount_Unauthorized() throws Exception {
			mockMvc.perform(get("/api/favorites/count"))
				.andExpect(status().isUnauthorized());
		}
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