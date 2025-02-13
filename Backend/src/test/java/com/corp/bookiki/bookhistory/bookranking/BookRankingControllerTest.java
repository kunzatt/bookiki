package com.corp.bookiki.bookhistory.bookranking;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.corp.bookiki.bookhistory.controller.BookRankingController;
import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.service.BookRankingService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;

@WebMvcTest(BookRankingController.class)
@Import({SecurityConfig.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
class BookRankingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookRankingService bookRankingService;

	private BookRankingResponse testBookRanking;

	@BeforeEach
	void setUp() {
		testBookRanking = new BookRankingResponse(
			1,
			"Test Book",
			"Test Author",
			100,
			"test-image.jpg",
			5L
		);
	}

	@Test
	@DisplayName("도서 랭킹 조회 성공")
	void getBookRanking_Success() throws Exception {
		// Given
		List<BookRankingResponse> rankings = List.of(testBookRanking);
		given(bookRankingService.getBookRanking()).willReturn(rankings);

		// When & Then
		mockMvc.perform(get("/api/books/ranking")
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
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
}