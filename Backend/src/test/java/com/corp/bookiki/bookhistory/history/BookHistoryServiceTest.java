package com.corp.bookiki.bookhistory.history;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class BookHistoryServiceTest {
	@InjectMocks
	private BookHistoryService bookHistoryService;

	@Mock
	private BookHistoryRepository bookHistoryRepository;

	private UserEntity testUser;
	private BookItemEntity testBookItem;
	private BookHistoryEntity testBookHistory;
	private AuthUser testAuthUser;
	private Pageable testPageable;
	private LocalDateTime now;

	@BeforeEach
	void setUp() {
		now = LocalDateTime.now();

		BookInformationEntity bookInfo = BookInformationEntity.builder()
			.title("Test Book")
			.author("Test Author")
			.isbn("1234567890")
			.build();

		testBookItem = BookItemEntity.builder()
			.bookInformation(bookInfo)
			.bookStatus(BookStatus.AVAILABLE)
			.build();
		ReflectionTestUtils.setField(testBookItem,"id", 1);

		testUser = UserEntity.builder()
			.email("test@example.com")
			.userName("Test User")
			.companyId("CORP001")
			.role(Role.USER)
			.provider(Provider.BOOKIKI)
			.build();
		ReflectionTestUtils.setField(testUser,"id", 23232);

		testBookHistory = BookHistoryEntity.builder()
			.bookItem(testBookItem)
			.user(testUser)
			.borrowedAt(now)
			.returnedAt(null)
			.overdue(false)
			.build();
		ReflectionTestUtils.setField(testBookHistory,"id", 1);

		testAuthUser = AuthUser.builder()
			.id(testUser.getId())
			.email(testUser.getEmail())
			.role(testUser.getRole())
			.build();

		testPageable = PageRequest.of(0, 20);
	}

	@Nested
	@DisplayName("관리자용 대출 기록 조회 테스트")
	class AdminBookHistoriesTest {
		@Test
		@DisplayName("성공적인 대출 기록 조회")
		void getAdminBookHistories_Success() {
			// Given
			LocalDate startDate = now.toLocalDate().minusDays(7);
			LocalDate endDate = now.toLocalDate();

			Page<BookHistoryEntity> mockPage = new PageImpl<>(List.of(testBookHistory));

			given(bookHistoryRepository.findAllBookHistoriesForAdmin(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				any(),
				any(),
				any(),
				eq(testPageable)
			)).willReturn(mockPage);

			// When
			Page<BookHistoryResponse> result = bookHistoryService.getAdminBookHistories(
				startDate, endDate,
				testUser.getUserName(),
				testUser.getCompanyId(),
				false,
				testPageable
			);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).getUserId()).isEqualTo(testUser.getId());

			verify(bookHistoryRepository).findAllBookHistoriesForAdmin(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(testUser.getUserName()),
				eq(testUser.getCompanyId()),
				eq(false),
				eq(testPageable)
			);
		}

		@Test
		@DisplayName("대출 기록 조회 중 리포지토리 오류 발생")
		void getAdminBookHistories_RepositoryError() {
			// Given
			LocalDate startDate = now.toLocalDate().minusDays(7);
			LocalDate endDate = now.toLocalDate();

			given(bookHistoryRepository.findAllBookHistoriesForAdmin(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				any(),
				any(),
				any(),
				eq(testPageable)
			)).willThrow(new RuntimeException("Database error"));

			// When & Then
			assertThatThrownBy(() ->
				bookHistoryService.getAdminBookHistories(
					startDate, endDate, null, null, null, testPageable
				)
			)
				.isInstanceOf(BookHistoryException.class)
				.hasMessageContaining(ErrorCode.HISTORY_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("사용자 대출 기록 조회 테스트")
	class UserBookHistoriesTest {
		@Test
		@DisplayName("성공적인 사용자 대출 기록 조회")
		void getUserBookHistories_Success() {
			// Given
			LocalDate startDate = now.toLocalDate().minusDays(7);
			LocalDate endDate = now.toLocalDate();

			Page<BookHistoryEntity> mockPage = new PageImpl<>(List.of(testBookHistory));

			given(bookHistoryRepository.findAllForUser(
				eq(testUser.getId()),
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(false),
				eq(testPageable)
			)).willReturn(mockPage);

			// When
			Page<BookHistoryResponse> result = bookHistoryService.getUserBookHistories(
				testUser.getId(), startDate, endDate, false, testPageable
			);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).getUserId()).isEqualTo(testUser.getId());

			verify(bookHistoryRepository).findAllForUser(
				eq(testUser.getId()),
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(false),
				eq(testPageable)
			);
		}

		@Test
		@DisplayName("사용자 대출 기록 조회 중 리포지토리 오류 발생")
		void getUserBookHistories_RepositoryError() {
			// Given
			LocalDate startDate = now.toLocalDate().minusDays(7);
			LocalDate endDate = now.toLocalDate();

			given(bookHistoryRepository.findAllForUser(
				eq(testUser.getId()),
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(false),
				eq(testPageable)
			)).willThrow(new RuntimeException("Database error"));

			// When & Then
			assertThatThrownBy(() ->
				bookHistoryService.getUserBookHistories(
					testUser.getId(), startDate, endDate, false, testPageable
				)
			)
				.isInstanceOf(BookHistoryException.class)
				.hasMessageContaining(ErrorCode.HISTORY_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("현재 대출 중인 도서 조회 테스트")
	class CurrentBorrowedBooksTest {
		@Test
		@DisplayName("성공적인 현재 대출 도서 조회")
		void getCurrentBorrowedBooks_Success() {
			// Given
			given(bookHistoryRepository.findCurrentBorrowsByUserId(
				eq(testUser.getId()),
				isNull()
			)).willReturn(List.of(testBookHistory));

			// When
			List<BookHistoryResponse> result = bookHistoryService.getCurrentBorrowedBooks(
				testUser.getId(), null
			);

			// Then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getUserId()).isEqualTo(testUser.getId());

			verify(bookHistoryRepository).findCurrentBorrowsByUserId(
				eq(testUser.getId()),
				isNull()
			);
		}

		@Test
		@DisplayName("현재 대출 도서 조회 중 리포지토리 오류 발생")
		void getCurrentBorrowedBooks_RepositoryError() {
			// Given
			given(bookHistoryRepository.findCurrentBorrowsByUserId(
				eq(testUser.getId()),
				isNull()
			)).willThrow(new RuntimeException("Database error"));

			// When & Then
			assertThatThrownBy(() ->
				bookHistoryService.getCurrentBorrowedBooks(
					testUser.getId(), null
				)
			)
				.isInstanceOf(BookHistoryException.class)
				.hasMessageContaining(ErrorCode.HISTORY_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("도서 대출 상태 확인 테스트")
	class BookBorrowStatusTest {
		@Test
		@DisplayName("특정 도서 대출 상태 확인 - 대출 중")
		void isBookCurrentlyBorrowed_Success() {
			// Given
			given(bookHistoryRepository.findCurrentBorrowByBookItem(testBookItem))
				.willReturn(Optional.of(testBookHistory));

			// When
			boolean result = bookHistoryService.isBookCurrentlyBorrowed(testBookItem);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("특정 도서 대출 상태 확인 - 대출 중 아님")
		void isBookCurrentlyBorrowed_NotBorrowed() {
			// Given
			given(bookHistoryRepository.findCurrentBorrowByBookItem(testBookItem))
				.willReturn(Optional.empty());

			// When
			boolean result = bookHistoryService.isBookCurrentlyBorrowed(testBookItem);

			// Then
			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("연체 상태 확인 테스트")
	class OverdueStatusTest {
		@Test
		@DisplayName("특정 대출 기록의 연체 상태 확인 - 연체")
		void isOverdue_Success() {
			// Given
			testBookHistory = BookHistoryEntity.builder()
				.overdue(true)
				.build();

			given(bookHistoryRepository.findById(1))
				.willReturn(Optional.of(testBookHistory));

			// When
			boolean result = bookHistoryService.isOverdue(1);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("특정 대출 기록의 연체 상태 확인 - 대출 기록 없음")
		void isOverdue_NotFound() {
			// Given
			given(bookHistoryRepository.findById(1))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> bookHistoryService.isOverdue(1))
				.isInstanceOf(BookHistoryException.class)
				.hasMessageContaining(ErrorCode.HISTORY_NOT_FOUND.getMessage());
		}
	}
}