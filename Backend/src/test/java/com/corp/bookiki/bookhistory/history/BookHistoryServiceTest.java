package com.corp.bookiki.bookhistory.history;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class BookHistoryServiceTest {
	@InjectMocks
	private BookHistoryService bookHistoryService;

	@Mock
	private BookHistoryRepository bookHistoryRepository;

	@Nested
	@DisplayName("도서 대출 기록 조회 테스트")
	class GetBookHistories {
		@Test
		@DisplayName("정상적인 기간 조회 시 성공")
		void getBookHistories_Success() {
			// given
			LocalDate startDate = LocalDate.now().minusDays(7);
			LocalDate endDate = LocalDate.now();
			String keyword = "Spring";
			Pageable pageable = PageRequest.of(0, 10);

			BookInformationEntity bookInfo = BookInformationEntity.builder()
				.title("Spring Boot in Action")
				.author("Craig Walls")
				.isbn("978-1617292545")
				.build();

			BookItemEntity bookItem = mock(BookItemEntity.class);
			when(bookItem.getId()).thenReturn(1);
			when(bookItem.getBookInformation()).thenReturn(bookInfo);

			UserEntity user = UserEntity.builder()
				.email("test@bookiki.com")
				.password("password")
				.companyId("EMP001")
				.userName("Test User")
				.role(Role.USER)
				.provider(Provider.BOOKIKI)
				.build();

			BookHistoryEntity entity = mock(BookHistoryEntity.class);
			when(entity.getBookItem()).thenReturn(bookItem);
			when(entity.getUser()).thenReturn(user);
			when(entity.getBorrowedAt()).thenReturn(LocalDateTime.now());

			Page<BookHistoryEntity> page = new PageImpl<>(List.of(entity));

			given(bookHistoryRepository.searchBookHistoryWithCount(
				startDate.atStartOfDay(),
				endDate.atTime(23, 59, 59),
				keyword,
				pageable
			)).willReturn(page);

			// when
			Page<BookHistoryResponse> result = bookHistoryService.getBookHistories(
				startDate, endDate, keyword, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(1);
			verify(bookHistoryRepository).searchBookHistoryWithCount(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(keyword),
				eq(pageable)
			);
		}

		@Test
		@DisplayName("repository 예외 발생 시 BookHistoryException 발생")
		void getBookHistories_WhenRepositoryThrowsException_ThenThrowBookHistoryException() {
			// given
			LocalDate startDate = LocalDate.now().minusDays(7);
			LocalDate endDate = LocalDate.now();
			Pageable pageable = PageRequest.of(0, 10);

			given(bookHistoryRepository.searchBookHistoryWithCount(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				any(),
				any()
			)).willThrow(new RuntimeException("Database error"));

			// when & then
			assertThatThrownBy(() ->
				bookHistoryService.getBookHistories(startDate, endDate, null, pageable)
			).isInstanceOf(BookHistoryException.class);
		}
	}

	@Nested
	@DisplayName("사용자별 대출 기록 조회 테스트")
	class GetUserBookHistories {
		@Test
		@DisplayName("사용자별 대출 기록 정상 조회")
		void getUserBookHistories_Success() {
			// given
			Integer userId = 1;
			LocalDate startDate = LocalDate.now().minusDays(7);
			LocalDate endDate = LocalDate.now();
			String keyword = "Spring";
			Pageable pageable = PageRequest.of(0, 10);

			BookInformationEntity bookInfo = BookInformationEntity.builder()
				.title("Spring Boot in Action")
				.author("Craig Walls")
				.isbn("978-1617292545")
				.build();

			BookItemEntity bookItem = mock(BookItemEntity.class);
			when(bookItem.getId()).thenReturn(1);
			when(bookItem.getBookInformation()).thenReturn(bookInfo);

			UserEntity user = UserEntity.builder()
				.email("test@bookiki.com")
				.password("password")
				.companyId("EMP001")
				.userName("Test User")
				.role(Role.USER)
				.provider(Provider.BOOKIKI)
				.build();

			BookHistoryEntity entity = mock(BookHistoryEntity.class);
			when(entity.getBookItem()).thenReturn(bookItem);
			when(entity.getUser()).thenReturn(user);
			when(entity.getBorrowedAt()).thenReturn(LocalDateTime.now());

			Page<BookHistoryEntity> page = new PageImpl<>(List.of(entity));

			given(bookHistoryRepository.searchUserBookHistoryWithCount(
				eq(userId),
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(keyword),
				eq(pageable)
			)).willReturn(page);

			// when
			Page<BookHistoryResponse> result = bookHistoryService.getUserBookHistories(
				userId, startDate, endDate, keyword, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("현재 대출 상태 확인 테스트")
	class IsBookCurrentlyBorrowed {
		@Test
		@DisplayName("도서 대출 상태 확인 성공")
		void isBookCurrentlyBorrowed_Success() {
			// given
			BookItemEntity bookItem = mock(BookItemEntity.class);
			BookHistoryEntity history = mock(BookHistoryEntity.class);
			given(bookHistoryRepository.findCurrentBorrowByBookItem(bookItem))
				.willReturn(Optional.of(history));

			// when
			boolean result = bookHistoryService.isBookCurrentlyBorrowed(bookItem);

			// then
			assertThat(result).isTrue();
		}
	}

	@Nested
	@DisplayName("사용자 현재 대출 목록 조회 테스트")
	class GetCurrentBorrowsByUser {
		@Test
		@DisplayName("사용자 현재 대출 목록 조회 성공")
		void getCurrentBorrowsByUser_Success() {
			// given
			UserEntity user = UserEntity.builder()
				.email("test@bookiki.com")
				.password("password")
				.companyId("EMP001")
				.userName("Test User")
				.role(Role.USER)
				.provider(Provider.BOOKIKI)
				.build();

			BookInformationEntity bookInfo = BookInformationEntity.builder()
				.title("Spring Boot in Action")
				.author("Craig Walls")
				.isbn("978-1617292545")
				.build();

			BookItemEntity bookItem = mock(BookItemEntity.class);
			when(bookItem.getId()).thenReturn(1);
			when(bookItem.getBookInformation()).thenReturn(bookInfo);

			BookHistoryEntity history = mock(BookHistoryEntity.class);
			when(history.getBookItem()).thenReturn(bookItem);
			when(history.getUser()).thenReturn(user);
			when(history.getBorrowedAt()).thenReturn(LocalDateTime.now());

			given(bookHistoryRepository.findCurrentBorrowsByUser(user))
				.willReturn(List.of(history));

			// when
			List<BookHistoryResponse> result = bookHistoryService.getCurrentBorrowsByUser(user);

			// then
			assertThat(result).hasSize(1);
		}
	}
}