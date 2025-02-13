package com.corp.bookiki.bookhistory.bookranking;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookhistory.service.BookRankingService;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;

@ExtendWith(MockitoExtension.class)
class BookRankingServiceTest {
	@InjectMocks
	private BookRankingService bookRankingService;

	@Mock
	private BookHistoryRepository bookHistoryRepository;

	@Nested
	@DisplayName("도서 랭킹 조회 테스트")
	class GetBookRankingTest {

		@Test
		@DisplayName("성공적인 도서 랭킹 조회")
		void getBookRanking_Success() {
			// Given
			LocalDateTime now = LocalDateTime.now();

			BookInformationEntity testBookInfo = mock(BookInformationEntity.class);
			when(testBookInfo.getTitle()).thenReturn("Test Book");
			when(testBookInfo.getAuthor()).thenReturn("Test Author");
			when(testBookInfo.getCategory()).thenReturn(100);
			when(testBookInfo.getImage()).thenReturn("test-image.jpg");

			BookItemEntity testBookItem = mock(BookItemEntity.class);
			when(testBookItem.getId()).thenReturn(1);
			when(testBookItem.getBookInformation()).thenReturn(testBookInfo);

			List<BookHistoryEntity> bookHistories = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				BookHistoryEntity history = mock(BookHistoryEntity.class);
				when(history.getBorrowedAt()).thenReturn(now.minusDays(i));
				bookHistories.add(history);
			}

			when(testBookItem.getBookHistories()).thenReturn(bookHistories);
			List<BookItemEntity> mockBookItems = List.of(testBookItem);

			given(bookHistoryRepository.findBorrowedBooksFromBookItems(
				any(LocalDateTime.class),
				any(LocalDateTime.class)
			)).willReturn(mockBookItems);

			// When
			List<BookRankingResponse> result = bookRankingService.getBookRanking();

			// Then
			assertThat(result).hasSize(1);
			BookRankingResponse response = result.get(0);
			assertThat(response.getBookItemId()).isEqualTo(testBookItem.getId());
			assertThat(response.getTitle()).isEqualTo(testBookInfo.getTitle());
			assertThat(response.getAuthor()).isEqualTo(testBookInfo.getAuthor());
			assertThat(response.getCategory()).isEqualTo(testBookInfo.getCategory());
			assertThat(response.getImage()).isEqualTo(testBookInfo.getImage());
			assertThat(response.getBorrowCount()).isEqualTo(5);
		}

		@Test
		@DisplayName("대출 기록이 없는 경우의 랭킹 조회")
		void getBookRanking_WithNoHistory() {
			// Given
			BookInformationEntity testBookInfo = mock(BookInformationEntity.class);
			when(testBookInfo.getTitle()).thenReturn("Test Book");

			BookItemEntity testBookItem = mock(BookItemEntity.class);
			when(testBookItem.getBookInformation()).thenReturn(testBookInfo);
			when(testBookItem.getBookHistories()).thenReturn(Collections.emptyList());

			List<BookItemEntity> mockBookItems = List.of(testBookItem);

			given(bookHistoryRepository.findBorrowedBooksFromBookItems(
				any(LocalDateTime.class),
				any(LocalDateTime.class)
			)).willReturn(mockBookItems);

			// When
			List<BookRankingResponse> result = bookRankingService.getBookRanking();

			// Then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("기간 외 대출 기록만 있는 경우의 랭킹 조회")
		void getBookRanking_WithOnlyOutOfRangeHistory() {
			// Given
			LocalDateTime now = LocalDateTime.now();

			BookInformationEntity testBookInfo = mock(BookInformationEntity.class);
			when(testBookInfo.getTitle()).thenReturn("Test Book");

			BookItemEntity testBookItem = mock(BookItemEntity.class);
			when(testBookItem.getBookInformation()).thenReturn(testBookInfo);

			List<BookHistoryEntity> oldHistories = new ArrayList<>();
			BookHistoryEntity oldHistory = mock(BookHistoryEntity.class);
			when(oldHistory.getBorrowedAt()).thenReturn(now.minusMonths(2));
			oldHistories.add(oldHistory);

			when(testBookItem.getBookHistories()).thenReturn(oldHistories);
			List<BookItemEntity> mockBookItems = List.of(testBookItem);

			given(bookHistoryRepository.findBorrowedBooksFromBookItems(
				any(LocalDateTime.class),
				any(LocalDateTime.class)
			)).willReturn(mockBookItems);

			// When
			List<BookRankingResponse> result = bookRankingService.getBookRanking();

			// Then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("대출 데이터가 없을 경우 빈 리스트 반환")
		void getBookRanking_WhenNoData_ReturnsEmptyList() {
			// Given
			given(bookHistoryRepository.findBorrowedBooksFromBookItems(
				any(LocalDateTime.class),
				any(LocalDateTime.class)
			)).willReturn(Collections.emptyList());

			// When
			List<BookRankingResponse> result = bookRankingService.getBookRanking();

			// Then
			assertThat(result).isEmpty();
		}
	}
}