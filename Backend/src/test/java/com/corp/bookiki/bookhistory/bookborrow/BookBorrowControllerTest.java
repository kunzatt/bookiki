package com.corp.bookiki.bookhistory.bookborrow;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

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
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookhistory.controller.BookBorrowController;
import com.corp.bookiki.bookhistory.dto.BookBorrowRequest;
import com.corp.bookiki.bookhistory.dto.BookBorrowResponse;
import com.corp.bookiki.bookhistory.service.BookBorrowService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(BookBorrowController.class)
@AutoConfigureMockMvc
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@DisplayName("도서 대출 컨트롤러 테스트")
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
class BookBorrowControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private BookBorrowService bookBorrowService;

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
	@DisplayName("도서 대출 API 테스트")
	class BorrowBook {

		@Test
		@WithMockUser
		@DisplayName("정상적인 도서 대출 요청 시 성공")
		void borrowBook_WhenValidRequest_ThenReturnsOk() throws Exception {
			// given
			BookBorrowRequest request = new BookBorrowRequest();
			request.setUserId(100);
			request.setBookItemId(200);

			BookBorrowResponse response = BookBorrowResponse.builder()
				.id(1)
				.userId(request.getUserId())
				.bookItemId(request.getBookItemId())
				.borrowedAt(LocalDateTime.now())
				.build();

			given(bookBorrowService.borrowBook(any(BookBorrowRequest.class)))
				.willReturn(response);
			log.info("테스트 요청 생성: userId={}, bookItemId={}",
				request.getUserId(), request.getBookItemId());

			// when & then
			mockMvc.perform(post("/books/borrow")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.userId").value(request.getUserId()))
				.andExpect(jsonPath("$.bookItemId").value(request.getBookItemId()))
				.andExpect(jsonPath("$.borrowedAt").exists());

			verify(bookBorrowService).borrowBook(any(BookBorrowRequest.class));
			log.info("도서 대출 테스트 성공적으로 완료");
		}

		@Test
		@WithMockUser
		@DisplayName("존재하지 않는 도서 대출 요청 시 404 반환")
		void borrowBook_WhenBookNotFound_ThenReturnsNotFound() throws Exception {
			// given
			BookBorrowRequest request = new BookBorrowRequest();
			request.setUserId(100);
			request.setBookItemId(999);

			given(bookBorrowService.borrowBook(any(BookBorrowRequest.class)))
				.willThrow(new BookHistoryException(ErrorCode.BOOK_ITEM_NOT_FOUND));
			log.info("존재하지 않는 도서 대출 요청 테스트 시작: bookItemId={}", request.getBookItemId());

			// when & then
			mockMvc.perform(post("/books/borrow")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());

			log.info("존재하지 않는 도서 대출 요청 테스트 완료");
		}
	}
}