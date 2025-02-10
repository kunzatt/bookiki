package com.corp.bookiki.bookhistory.bookborrow;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookhistory.controller.BookBorrowController;
import com.corp.bookiki.bookhistory.dto.BookBorrowResponse;
import com.corp.bookiki.bookhistory.service.BookBorrowService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@WebMvcTest(BookBorrowController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
	CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("도서 대출 컨트롤러 테스트")
@Slf4j
class BookBorrowControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private BookBorrowService bookBorrowService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	private UserEntity testUserEntity;
	private String testEmail;
	private Authentication auth;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();

		testEmail = "test@example.com";
		testUserEntity = UserEntity.builder()
			.email(testEmail)
			.userName("Test User")
			.role(Role.USER)
			.build();
		ReflectionTestUtils.setField(testUserEntity, "id", 2);

		given(userRepository.findByEmail(eq(testEmail)))
			.willReturn(Optional.of(testUserEntity));

		auth = new UsernamePasswordAuthenticationToken(
			testEmail,
			null,
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		log.info("MockMvc 및 테스트 사용자 설정이 완료되었습니다.");
	}

	@Nested
	@DisplayName("도서 대출 API 테스트")
	class BorrowBook {

		@Test
		@WithMockUser
		@DisplayName("정상적인 도서 대출 요청 시 성공")
		void borrowBook_WhenValidRequest_ThenReturnsOk() throws Exception {
			// given
			Integer bookItemId = 200;

			BookBorrowResponse response = BookBorrowResponse.builder()
				.id(1)
				.userId(testUserEntity.getId())
				.bookItemId(bookItemId)
				.borrowedAt(LocalDateTime.now())
				.build();

			given(bookBorrowService.borrowBook(eq(testUserEntity.getId()), eq(bookItemId)))
				.willReturn(response);
			log.info("테스트 요청 생성: userId={}, bookItemId={}",
				testUserEntity.getId(), bookItemId);

			// when & then
			mockMvc.perform(post("/api/books/borrow")
					.with(csrf())
					.with(authentication(auth))
					.param("bookItemId", String.valueOf(bookItemId))
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.userId").value(testUserEntity.getId()))
				.andExpect(jsonPath("$.bookItemId").value(bookItemId))
				.andExpect(jsonPath("$.borrowedAt").exists());

			verify(bookBorrowService).borrowBook(eq(testUserEntity.getId()), eq(bookItemId));
			log.info("도서 대출 테스트 성공적으로 완료");
		}

		@Test
		@WithMockUser
		@DisplayName("존재하지 않는 도서 대출 요청 시 404 반환")
		void borrowBook_WhenBookNotFound_ThenReturnsNotFound() throws Exception {
			// given
			Integer bookItemId = 999;

			given(bookBorrowService.borrowBook(eq(testUserEntity.getId()), eq(bookItemId)))
				.willThrow(new BookHistoryException(ErrorCode.BOOK_ITEM_NOT_FOUND));
			log.info("존재하지 않는 도서 대출 요청 테스트 시작: bookItemId={}", bookItemId);

			// when & then
			mockMvc.perform(post("/api/books/borrow")
					.with(csrf())
					.with(authentication(auth))
					.param("bookItemId", String.valueOf(bookItemId))
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

			log.info("존재하지 않는 도서 대출 요청 테스트 완료");
		}

		@Test
		@WithMockUser
		@DisplayName("대출 한도 초과 시 400 반환")
		void borrowBook_WhenBorrowLimitExceeded_ThenReturnsBadRequest() throws Exception {
			// given
			Integer bookItemId = 400;

			given(bookBorrowService.borrowBook(eq(testUserEntity.getId()), eq(bookItemId)))
				.willThrow(new BookHistoryException(ErrorCode.BORROW_LIMIT_EXCEEDED));
			log.info("대출 한도 초과 테스트 시작: userId={}", testUserEntity.getId());

			// when & then
			mockMvc.perform(post("/api/books/borrow")
					.with(csrf())
					.with(authentication(auth))
					.param("bookItemId", String.valueOf(bookItemId))
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

			log.info("대출 한도 초과 테스트 완료");
		}
	}
}