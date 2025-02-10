package com.corp.bookiki.bookhistory.history;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookhistory.controller.BookHistoryController;
import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.PeriodType;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;

import lombok.extern.slf4j.Slf4j;

@WebMvcTest(BookHistoryController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
	CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("도서 대출 기록 컨트롤러 테스트")
@Slf4j
class BookHistoryControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookHistoryService bookHistoryService;

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

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("관리자의 도서 대출 기록 전체 조회 성공")
	void getAdminBookHistories_WhenValidRequest_ThenReturnsOk() throws Exception {

		Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

		LocalDate startDate = LocalDate.now().minusMonths(1);
		LocalDate endDate = LocalDate.now();

		BookHistoryResponse response = BookHistoryResponse.builder()
			.id(1)
			.bookItemId(100)
			.userId(200)
			.borrowedAt(LocalDateTime.now())
			.returnedAt(LocalDateTime.now().plusDays(14))
			.bookTitle("테스트 도서")
			.bookAuthor("테스트 저자")
			.overdue(false)
			.build();

		Page<BookHistoryResponse> page = new PageImpl<>(List.of(response), pageable, 1);

		given(bookHistoryService.getAdminBookHistories(
			eq(startDate),
			eq(endDate),
			eq("홍길동"),        // 검색 조건으로만 사용
			eq("CORP123"),      // 검색 조건으로만 사용
			eq(false),
			any(Pageable.class)
		)).willReturn(page);

		mockMvc.perform(get("/api/admin/book-histories")
				.param("periodType", PeriodType.LAST_MONTH.name())
				.param("startDate", startDate.toString())
				.param("endDate", endDate.toString())
				.param("userName", "홍길동")
				.param("companyId", "CORP123")
				.param("overdue", "false")
				.param("page", "0")
				.param("size", "10")
				.param("sort", "id,desc")
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].id").value(1))
			.andExpect(jsonPath("$.content[0].bookItemId").value(100))
			.andExpect(jsonPath("$.content[0].userId").value(200))
			.andExpect(jsonPath("$.content[0].bookTitle").value("테스트 도서"))
			.andExpect(jsonPath("$.content[0].bookAuthor").value("테스트 저자"))
			.andExpect(jsonPath("$.content[0].overdue").value(false));
	}

	@Test
	@WithMockUser
	@DisplayName("일반 사용자의 도서 대출 기록 조회 성공")
	void getUserBookHistories_WhenValidRequest_ThenReturnsOk() throws Exception {
		BookHistoryResponse response = BookHistoryResponse.builder()
			.id(1)
			.bookItemId(100)
			.userId(testUserEntity.getId())
			.borrowedAt(LocalDateTime.now())
			.returnedAt(LocalDateTime.now().plusDays(14))
			.bookTitle("테스트 도서")
			.bookAuthor("테스트 저자")
			.overdue(false)
			.build();

		LocalDate startDate = LocalDate.now().minusMonths(1);
		LocalDate endDate = LocalDate.now();

		Pageable pageable = PageRequest.of(0, 20, Sort.by("borrowedAt").descending());
		Page<BookHistoryResponse> page = new PageImpl<>(List.of(response), pageable, 1);

		given(bookHistoryService.getUserBookHistories(
			eq(testUserEntity.getId()),  // 실제 사용자 ID
			eq(startDate),
			eq(endDate),
			eq(false),
			any(Pageable.class)
		)).willReturn(page);

		mockMvc.perform(get("/api/user/book-histories")
				.param("periodType", PeriodType.LAST_MONTH.name())
				.param("startDate", startDate.toString())
				.param("endDate", endDate.toString())
				.param("overdue", "false")
				.param("page", "0")
				.param("size", "20")
				.param("sort", "borrowedAt,desc")
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(auth)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].id").value(1))
			.andExpect(jsonPath("$.content[0].bookItemId").value(100))
			.andExpect(jsonPath("$.content[0].userId").value(2))
			.andExpect(jsonPath("$.content[0].bookTitle").value("테스트 도서"))
			.andExpect(jsonPath("$.content[0].bookAuthor").value("테스트 저자"))
			.andExpect(jsonPath("$.content[0].overdue").value(false));
	}

	@Test
	@WithMockUser(roles = "USER")
	@DisplayName("권한 없는 사용자의 관리자 API 접근 시 실패")
	void getAdminBookHistories_WhenUnauthorized_ThenReturnsForbidden() throws Exception {
		mockMvc.perform(get("/api/admin/book-histories")
				.param("periodType", PeriodType.LAST_MONTH.name())
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser
	@DisplayName("현재 대출 중인 도서 목록 조회 성공")
	void getCurrentBorrowedBooks_WhenValidRequest_ThenReturnsOk() throws Exception {
		BookHistoryResponse response = BookHistoryResponse.builder()
			.id(1)
			.bookItemId(100)
			.userId(testUserEntity.getId())
			.borrowedAt(LocalDateTime.now())
			.returnedAt(LocalDateTime.now().plusDays(14))
			.bookTitle("테스트 도서")
			.bookAuthor("테스트 저자")
			.overdue(false)
			.build();

		List<BookHistoryResponse> responses = List.of(response);

		given(bookHistoryService.getCurrentBorrowedBooks(testUserEntity.getId(), false))
			.willReturn(responses);

		mockMvc.perform(get("/api/user/book-histories/current")
				.param("onlyOverdue", "false")
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(auth)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[0].id").value(1))
			.andExpect(jsonPath("$.[0].bookItemId").value(100))
			.andExpect(jsonPath("$.[0].userId").value(2))
			.andExpect(jsonPath("$.[0].bookTitle").value("테스트 도서"))
			.andExpect(jsonPath("$.[0].bookAuthor").value("테스트 저자"))
			.andExpect(jsonPath("$.[0].overdue").value(false));
	}

	@Test
	@WithMockUser
	@DisplayName("잘못된 기간 타입으로 조회 시 실패")
	void getUserBookHistories_WhenInvalidPeriodType_ThenReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/user/book-histories")
				.param("periodType", "INVALID_PERIOD")
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isInternalServerError());
	}
}