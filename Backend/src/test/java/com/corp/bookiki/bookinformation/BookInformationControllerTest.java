package com.corp.bookiki.bookinformation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookhistory.controller.BookHistoryController;
import com.corp.bookiki.bookinformation.controller.BookInformationController;
import com.corp.bookiki.bookinformation.dto.BookInformationResponse;
import com.corp.bookiki.bookinformation.service.BookInformationService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookInformationException;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.CustomUserDetailsService;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@WebMvcTest(BookInformationController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
	CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("도서 정보 컨트롤러 테스트")
@Slf4j
class BookInformationControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private BookInformationService bookInformationService;

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
	@DisplayName("정상적인 ISBN으로 도서 정보 조회 시 성공")
	void getBookInformation_WhenValidIsbn_ReturnsOk() throws Exception {
		String isbn = "9788937460470";
		BookInformationResponse response = new BookInformationResponse();
		response.setIsbn(isbn);
		response.setTitle("테스트 도서");

		log.info("테스트 도서 정보 생성: ISBN={}", isbn);
		given(bookInformationService.addBookInformationByIsbn(isbn)).willReturn(response);

		mockMvc.perform(get("/api/books/info/isbn/{isbn}", isbn)
				.cookie(getUserJwtCookie()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isbn").value(isbn));

		log.info("도서 정보 조회 테스트 완료");
	}

	@Test
	@WithMockUser
	@DisplayName("잘못된 ISBN으로 조회 시 400 에러 반환")
	void getBookInformation_WhenInvalidIsbn_ReturnsBadRequest() throws Exception {
		String isbn = "invalid-isbn";
		given(bookInformationService.addBookInformationByIsbn(anyString()))
			.willThrow(new BookInformationException(ErrorCode.INVALID_ISBN));

		mockMvc.perform(get("/api/books/info/isbn/{isbn}", isbn)
				.cookie(getUserJwtCookie()))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	@DisplayName("도서 정보를 찾을 수 없을 때 404 에러 반환")
	void getBookInformation_WhenNotFound_ReturnsNotFound() throws Exception {
		String isbn = "9788937460470";
		given(bookInformationService.addBookInformationByIsbn(anyString()))
			.willThrow(new BookInformationException(ErrorCode.BOOK_INFO_NOT_FOUND));

		mockMvc.perform(get("/api/books/info/isbn/{isbn}", isbn)
				.cookie(getUserJwtCookie()))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	@DisplayName("id로 도서 정보 조회 시 성공")
	void getBookInformation_WhenValidId_ReturnsOk() throws Exception {
		int id = 1;
		BookInformationResponse response = new BookInformationResponse();
		response.setId(id);
		response.setTitle("테스트 도서");

		given(bookInformationService.getBookInformation(id)).willReturn(response);

		mockMvc.perform(get("/api/books/info/{id}", id)
				.cookie(getUserJwtCookie()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id));
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