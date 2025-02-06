package com.corp.bookiki.bookhistory.history;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookhistory.controller.BookHistoryController;
import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.PeriodType;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.util.CookieUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(BookHistoryController.class)
@AutoConfigureMockMvc
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@DisplayName("도서 대출 기록 컨트롤러 테스트")
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
class BookHistoryControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private BookHistoryService bookHistoryService;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
	}

	@Nested
	@DisplayName("도서 대출 기록 조회 API 테스트")
	class GetBookHistories {
		@Test
		@WithMockUser
		@DisplayName("기간 타입이 LAST_WEEK인 경우 정상 조회")
		void getBookHistories_WhenPeriodTypeIsLastWeek_ThenReturnsOk() throws Exception {
			// given
			LocalDate endDate = LocalDate.now();
			LocalDate startDate = endDate.minusWeeks(1);

			BookHistoryResponse response = BookHistoryResponse.builder()
				.id(1)
				.bookItemId(100)
				.userId(200)
				.borrowedAt(LocalDateTime.now())
				.returnedAt(LocalDateTime.now().plusDays(14))
				.build();

			Page<BookHistoryResponse> page = new PageImpl<>(
				List.of(response),
				PageRequest.of(0, 20),
				1
			);

			given(bookHistoryService.getAdminBookHistories(
				startDate,
				endDate,
				null,
				null,
				null,
				PageRequest.of(0, 20)
			)).willReturn(page);

			// when & then
			mockMvc.perform(get("/api/users/books")
					.param("periodType", PeriodType.LAST_WEEK.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").exists())
				.andExpect(jsonPath("$.content[0].bookItemId").value(100))
				.andExpect(jsonPath("$.content[0].userId").value(200))
				.andExpect(jsonPath("$.content[0].borrowedAt").exists())
				.andExpect(jsonPath("$.content[0].returnedAt").exists());
		}

		@Test
		@WithMockUser
		@DisplayName("사용자 지정 기간으로 조회 시 성공")
		void getBookHistories_WhenCustomPeriod_ThenReturnsOk() throws Exception {
			// given
			LocalDate startDate = LocalDate.now().minusMonths(1);
			LocalDate endDate = LocalDate.now();
			String keyword = "Spring";

			Page<BookHistoryResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

			given(bookHistoryService.getUserBookHistories(
				any(AuthUser.class),
				startDate,
				endDate,
				null,
				PageRequest.of(0, 20)
			)).willReturn(page);

			// when & then
			mockMvc.perform(get("/api/users/books")
					.param("periodType", PeriodType.CUSTOM.toString())
					.param("startDate", startDate.toString())
					.param("endDate", endDate.toString())
					.param("keyword", keyword))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
		}

		@Test
		@WithMockUser
		@DisplayName("CUSTOM 타입에서 시작일/종료일 누락 시 실패")
		void getBookHistories_WhenCustomPeriodWithoutDates_ThenReturnsBadRequest() throws Exception {
			// when & then
			mockMvc.perform(get("/api/users/books")
					.param("periodType", PeriodType.CUSTOM.toString()))
				.andExpect(status().isBadRequest());
		}
	}
}