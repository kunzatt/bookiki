package com.corp.bookiki.bookhistory.bookranking;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookhistory.controller.BookRankingController;
import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.service.BookRankingService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@WebMvcTest(BookRankingController.class)
@Import({SecurityConfig.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@Slf4j
class BookRankingControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookRankingService bookRankingService;

	private BookRankingResponse testBookRanking;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

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

		testBookRanking = new BookRankingResponse(
			1,
			"Test Book",
			"Test Author",
			100,
			"test-image.jpg",
			5L
		);

		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
	}

	@Test
	@DisplayName("도서 랭킹 조회 성공")
	void getBookRanking_Success() throws Exception {
		// Given
		List<BookRankingResponse> rankings = List.of(testBookRanking);
		given(bookRankingService.getBookRanking()).willReturn(rankings);

		// When & Then
		mockMvc.perform(get("/api/books/ranking")
				.cookie(getUserJwtCookie())
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].bookItemId").value(testBookRanking.getBookItemId()))
			.andExpect(jsonPath("$[0].title").value(testBookRanking.getTitle()))
			.andExpect(jsonPath("$[0].author").value(testBookRanking.getAuthor()))
			.andExpect(jsonPath("$[0].category").value(testBookRanking.getCategory()))
			.andExpect(jsonPath("$[0].borrowCount").value(testBookRanking.getBorrowCount()));
	}

	@Test
	@DisplayName("랭킹 데이터가 없을 경우 404 응답")
	void getBookRanking_WhenNoData_Returns404() throws Exception {
		// Given
		given(bookRankingService.getBookRanking())
			.willThrow(new BookHistoryException(ErrorCode.NO_RANKING_DATA));

		// When & Then
		mockMvc.perform(get("/api/books/ranking")
				.cookie(getUserJwtCookie())
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isNotFound());
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