package com.corp.bookiki.bookhistory.history;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;

import lombok.extern.slf4j.Slf4j;

@WebMvcTest(BookHistoryController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("도서 대출 기록 컨트롤러 테스트")
@Slf4j
class BookHistoryControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookHistoryService bookHistoryService;

	@MockBean
	private CurrentUserArgumentResolver currentUserArgumentResolver;

	@Autowired
	private UserRepository userRepository;

	private Pageable pageable;
	private Authentication adminAuth;
	private Authentication userAuth;
	private UserEntity userEntity;
	private UserEntity adminEntity;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();

		userEntity = UserEntity.builder()
			.email("user@test.com")
			.role(Role.USER)
			.build();
		ReflectionTestUtils.setField(userEntity, "id", 2);

		adminEntity = UserEntity.builder()
			.email("admin@test.com")
			.role(Role.ADMIN)
			.build();
		ReflectionTestUtils.setField(adminEntity, "id", 1);

		// UserRepository 모킹
		Mockito.when(userRepository.findByEmail(userEntity.getEmail())).thenReturn(Optional.of(userEntity));
		Mockito.when(userRepository.findByEmail(adminEntity.getEmail())).thenReturn(Optional.of(adminEntity));

		userAuth = new UsernamePasswordAuthenticationToken(
			userEntity.getEmail(),
			null,
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);

		adminAuth = new UsernamePasswordAuthenticationToken(
			adminEntity.getEmail(),
			null,
			List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
		);

		pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "borrowedAt"));

		Mockito.when(currentUserArgumentResolver.supportsParameter(any())).thenReturn(true);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("관리자의 도서 대출 기록 전체 조회 성공")
	void getAdminBookHistories_WhenValidRequest_ThenReturnsOk() throws Exception {
		Mockito.when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.thenReturn(adminEntity);

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

		Mockito.when(bookHistoryService.getAdminBookHistories(any(), any(), any(), any(), any(), any()))
			.thenReturn(page);

		mockMvc.perform(get("/api/admin/book-histories")
				.param("periodType", PeriodType.LAST_MONTH.name())
				.param("userName", "홍길동")
				.param("companyId", "CORP123")
				.param("overdue", "false")
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(adminAuth)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].id").value(1))
			.andExpect(jsonPath("$.content[0].bookItemId").value(100))
			.andExpect(jsonPath("$.content[0].userName").value("홍길동"))
			.andExpect(jsonPath("$.content[0].companyId").value("CORP123"));
	}

	@Test
	@WithMockUser
	@DisplayName("일반 사용자의 도서 대출 기록 조회 성공")
	void getUserBookHistories_WhenValidRequest_ThenReturnsOk() throws Exception {
		Mockito.when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.thenReturn(userEntity);

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

		Mockito.when(bookHistoryService.getUserBookHistories(any(), any(), any(), any(), any()))
			.thenReturn(page);

		// Perform test
		mockMvc.perform(get("/api/user/book-histories")
				.param("page", "0")
				.param("size", "20")
				.param("sort", "borrowedAt,desc")
				.param("periodType", PeriodType.LAST_MONTH.name())
				.param("overdue", "false")
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(userAuth)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].id").value(1))
			.andExpect(jsonPath("$.content[0].bookItemId").value(100));
	}

	@Test
	@WithMockUser
	@DisplayName("현재 대출 중인 도서 목록 조회 성공")
	void getCurrentBorrowedBooks_WhenValidRequest_ThenReturnsOk() throws Exception {
		Mockito.when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.thenReturn(userEntity);

		BookHistoryResponse response = BookHistoryResponse.builder()
			.id(1)
			.bookItemId(100)
			.userId(200)
			.borrowedAt(LocalDateTime.now())
			.bookTitle("테스트 도서")
			.bookAuthor("테스트 저자")
			.overdue(false)
			.build();

		List<BookHistoryResponse> responses = List.of(response);

		Mockito.when(bookHistoryService.getCurrentBorrowedBooks(any(), any()))
			.thenReturn(responses);

		mockMvc.perform(get("/api/user/book-histories/current")
				.param("onlyOverdue", "false")
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(userAuth)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].bookItemId").value(100));
	}

	@Test
	@WithMockUser
	@DisplayName("권한 없는 사용자의 관리자 API 접근 시 실패")
	void getAdminBookHistories_WhenUnauthorized_ThenReturnsForbidden() throws Exception {
		Mockito.when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.thenReturn(userEntity);

		mockMvc.perform(get("/api/admin/book-histories")
				.param("periodType", PeriodType.LAST_MONTH.name())
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(userAuth)))
			.andDo(print())
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser
	@DisplayName("잘못된 기간 타입으로 조회 시 실패")
	void getUserBookHistories_WhenInvalidPeriodType_ThenReturnsBadRequest() throws Exception {
		Mockito.when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.thenReturn(userEntity);

		mockMvc.perform(get("/api/user/book-histories")
				.param("periodType", "INVALID_PERIOD")
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(userAuth)))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}
}