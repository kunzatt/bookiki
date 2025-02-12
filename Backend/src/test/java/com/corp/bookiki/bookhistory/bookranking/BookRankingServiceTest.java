package com.corp.bookiki.bookhistory.bookranking;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookhistory.service.BookRankingService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;


@ExtendWith(MockitoExtension.class)
class BookRankingServiceTest {
	@InjectMocks
	private BookRankingService bookRankingService;

	@Mock
	private BookHistoryRepository bookHistoryRepository;

	private BookRankingResponse testBookRanking;
	private LocalDateTime now;

	@BeforeEach
	void setUp() {
		now = LocalDateTime.now();
		testBookRanking = new BookRankingResponse(
			1,
			"Test Book",
			"Test Author",
			100,
			"test-image.jpg",
			5L
		);
	}

	@Nested
	@DisplayName("도서 랭킹 조회 테스트")
	class GetBookRankingTest {

		@Test
		@DisplayName("성공적인 도서 랭킹 조회")
		void getBookRanking_Success() {
			// Given
			List<BookRankingResponse> mockRankings = List.of(testBookRanking);

			given(bookHistoryRepository.findTop10BorrowedBooksFromBookItems(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				isNull(),
				any(Limit.class)
			)).willReturn(mockRankings);

			// When
			List<BookRankingResponse> result = bookRankingService.getBookRanking();

			// Then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getBookItemId()).isEqualTo(testBookRanking.getBookItemId());
			assertThat(result.get(0).getBorrowCount()).isEqualTo(testBookRanking.getBorrowCount());
		}

		@Test
		@DisplayName("랭킹 데이터가 없을 경우 예외 발생")
		void getBookRanking_WhenNoData_ThrowsException() {
			// Given
			given(bookHistoryRepository.findTop10BorrowedBooksFromBookItems(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				isNull(),
				any(Limit.class)
			)).willReturn(Collections.emptyList());

			// When & Then
			assertThatThrownBy(() -> bookRankingService.getBookRanking())
				.isInstanceOf(BookHistoryException.class)
				.hasMessageContaining(ErrorCode.NO_RANKING_DATA.getMessage());
		}
	}
}