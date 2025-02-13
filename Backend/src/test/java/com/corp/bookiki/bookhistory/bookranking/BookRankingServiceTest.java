package com.corp.bookiki.bookhistory.bookranking;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookhistory.service.BookRankingService;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;

@ExtendWith(MockitoExtension.class)
class BookRankingServiceTest {
	@InjectMocks
	private BookRankingService bookRankingService;

	@Mock
	private BookHistoryRepository bookHistoryRepository;

	@Mock
	private BookItemRepository bookItemRepository;

	@Mock
	private BookInformationEntity bookInformation;

	@Mock
	private BookItemEntity bookItem;

	@BeforeEach
	void setUp() {
		when(bookInformation.getAuthor()).thenReturn("Test Author");
		when(bookInformation.getCategory()).thenReturn(100);
		when(bookInformation.getImage()).thenReturn("test-image.jpg");
		when(bookItem.getBookInformation()).thenReturn(bookInformation);
	}

	@Test
	@DisplayName("성공적인 도서 랭킹 조회")
	void getBookRanking_Success() {
		// Given
		int bookItemId = 1;
		String title = "Test Book";
		long borrowCount = 5L;

		Object[] rankingRow = new Object[]{bookItemId, title, borrowCount};
		List<Object[]> rankingRows = new ArrayList<>();
		rankingRows.add(rankingRow);

		when(bookHistoryRepository.findTopMostBorrowedBooks(
			any(LocalDateTime.class),
			any(LocalDateTime.class)
		)).thenReturn(rankingRows);

		when(bookItemRepository.findByIdWithBookInformation(bookItemId))
			.thenReturn(Optional.of(bookItem));

		// When
		List<BookRankingResponse> result = bookRankingService.getBookRanking();

		// Then
		assertThat(result).hasSize(1);
		BookRankingResponse response = result.get(0);

		assertThat(response.getBookItemId()).isEqualTo(bookItemId);
		assertThat(response.getTitle()).isEqualTo(title);
		assertThat(response.getAuthor()).isEqualTo("Test Author");
		assertThat(response.getCategory()).isEqualTo(100);
		assertThat(response.getImage()).isEqualTo("test-image.jpg");
		assertThat(response.getBorrowCount()).isEqualTo(borrowCount);
	}
}