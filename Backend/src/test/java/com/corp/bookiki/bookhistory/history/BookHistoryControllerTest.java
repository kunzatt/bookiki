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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookhistory.controller.BookHistoryController;
import com.corp.bookiki.bookhistory.dto.BookHistoryRequest;
import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.PeriodType;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
		log.info("MockMvc 설정이 완료되었습니다.");
	}

	@Nested
	@DisplayName("도서 대출 기록 조회 API 테스트")
	class GetBookHistories {

		@Test
		@WithMockUser
		@DisplayName("기간 타입이 LAST_WEEK인 경우 정상 조회")
		void getBookHistories_WhenPeriodTypeIsLastWeek_ThenReturnsOk() throws Exception {
			// given
			BookHistoryRequest request = new BookHistoryRequest();
			request.setPeriodType(PeriodType.LAST_WEEK);

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

			given(bookHistoryService.getBookHistories(any(BookHistoryRequest.class), any(Pageable.class)))
				.willReturn(page);

			log.info("테스트 요청 생성: periodType={}", request.getPeriodType());

			// when & then
			mockMvc.perform(get("/api/book-histories")
					.param("periodType", request.getPeriodType().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").exists())
				.andExpect(jsonPath("$.content[0].bookItemId").value(100))
				.andExpect(jsonPath("$.content[0].userId").value(200))
				.andExpect(jsonPath("$.content[0].borrowedAt").exists())
				.andExpect(jsonPath("$.content[0].returnedAt").exists());

			verify(bookHistoryService).getBookHistories(any(BookHistoryRequest.class), any(Pageable.class));
			log.info("도서 대출 기록 조회 테스트 성공적으로 완료");
		}

		@Test
		@WithMockUser
		@DisplayName("사용자 지정 기간으로 조회 시 성공")
		void getBookHistories_WhenCustomPeriod_ThenReturnsOk() throws Exception {
			// given
			LocalDate startDate = LocalDate.now().minusMonths(1);
			LocalDate endDate = LocalDate.now();

			BookHistoryRequest request = new BookHistoryRequest();
			request.setPeriodType(PeriodType.CUSTOM);
			request.setStartDate(startDate);
			request.setEndDate(endDate);

			Page<BookHistoryResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

			given(bookHistoryService.getBookHistories(any(BookHistoryRequest.class), any(Pageable.class)))
				.willReturn(page);

			log.info("테스트 요청 생성: periodType={}, startDate={}, endDate={}",
				request.getPeriodType(), startDate, endDate);

			// when & then
			mockMvc.perform(get("/api/book-histories")
					.param("periodType", request.getPeriodType().toString())
					.param("startDate", startDate.toString())
					.param("endDate", endDate.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());

			verify(bookHistoryService).getBookHistories(any(BookHistoryRequest.class), any(Pageable.class));
			log.info("사용자 지정 기간 조회 테스트 성공적으로 완료");
		}

		@Test
		@WithMockUser
		@DisplayName("CUSTOM 타입에서 시작일/종료일 누락 시 실패")
		void getBookHistories_WhenCustomPeriodWithoutDates_ThenReturnsBadRequest() throws Exception {
			// given
			BookHistoryRequest request = new BookHistoryRequest();
			request.setPeriodType(PeriodType.CUSTOM);

			given(bookHistoryService.getBookHistories(any(BookHistoryRequest.class), any(Pageable.class)))
				.willThrow(new BookHistoryException(ErrorCode.INVALID_INPUT_VALUE));

			log.info("테스트 요청 생성: periodType={}, dates=null", request.getPeriodType());

			// when & then
			mockMvc.perform(get("/api/book-histories")
					.param("periodType", request.getPeriodType().toString()))
				.andExpect(status().isBadRequest());

			log.info("날짜 누락 테스트 성공적으로 완료");
		}

	}
}