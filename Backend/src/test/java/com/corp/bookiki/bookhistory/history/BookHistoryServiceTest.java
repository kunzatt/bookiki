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
import com.corp.bookiki.global.error.exception.UserException;
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

	@Nested
	@DisplayName("도서 대출 기록 조회 테스트")
	class GetBookHistories {
		@Test
		@DisplayName("관리자용 대출 기록 조회 성공")
		void getAdminBookHistories_Success() {
			// given
			LocalDate startDate = LocalDate.now().minusDays(7);
			LocalDate endDate = LocalDate.now();
			String userName = "Test User";
			String companyId = "EMP001";
			Boolean isOverdue = false;
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

			given(bookHistoryRepository.findAllForAdmin(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(userName),
				eq(companyId),
				eq(isOverdue),
				eq(pageable)
			)).willReturn(page);

			// when
			Page<BookHistoryResponse> result = bookHistoryService.getAdminBookHistories(
				startDate, endDate, userName, companyId, isOverdue, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(1);
			verify(bookHistoryRepository).findAllForAdmin(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(userName),
				eq(companyId),
				eq(isOverdue),
				eq(pageable)
			);
		}

		@Test
		@DisplayName("관리자 대출 기록 조회 중 오류 발생 시 BookHistoryException 발생")
		void getAdminBookHistories_RepositoryError() {
			// given
			LocalDate startDate = LocalDate.now().minusDays(7);
			LocalDate endDate = LocalDate.now();
			Pageable pageable = PageRequest.of(0, 10);

			given(bookHistoryRepository.findAllForAdmin(
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				any(),
				any(),
				any(),
				eq(pageable)
			)).willThrow(new RuntimeException("Database error"));

			// when & then
			assertThatThrownBy(() ->
				bookHistoryService.getAdminBookHistories(
					startDate,
					endDate,
					null,
					null,
					null,
					pageable
				)
			).isInstanceOf(BookHistoryException.class);
		}
	}

	@Nested
	@DisplayName("사용자별 대출 기록 조회 테스트")
	class GetUserBookHistories {
		@Test
		@DisplayName("사용자 대출 기록 조회 성공")
		void getUserBookHistories_Success() {
			// given
			AuthUser authUser = mock(AuthUser.class);
			when(authUser.getEmail()).thenReturn("test@bookiki.com");

			LocalDate startDate = LocalDate.now().minusDays(7);
			LocalDate endDate = LocalDate.now();
			Boolean overdue = false;
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

			given(bookHistoryRepository.findAllForUser(
				eq(authUser.getEmail()),
				any(LocalDateTime.class),
				any(LocalDateTime.class),
				eq(overdue),
				eq(pageable)
			)).willReturn(page);

			// when
			Page<BookHistoryResponse> result = bookHistoryService.getUserBookHistories(
				authUser, startDate, endDate, overdue, pageable);

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
	@DisplayName("현재 대출 목록 조회 테스트")
	class GetCurrentBorrowedBooks {
		@Test
		@DisplayName("사용자의 현재 대출 목록 조회 성공")
		void getCurrentBorrowedBooks_Success() {
			// given
			AuthUser authUser = mock(AuthUser.class);
			when(authUser.getEmail()).thenReturn("test@bookiki.com");

			BookInformationEntity bookInfo = BookInformationEntity.builder()
				.title("Spring Boot in Action")
				.author("Craig Walls")
				.isbn("978-1617292545")
				.build();

			BookItemEntity bookItem = mock(BookItemEntity.class);
			when(bookItem.getBookInformation()).thenReturn(bookInfo);

			UserEntity user = UserEntity.builder()
				.email("test@bookiki.com")
				.userName("Test User")
				.companyId("EMP001")
				.role(Role.USER)
				.provider(Provider.BOOKIKI)
				.build();

			BookHistoryEntity history = mock(BookHistoryEntity.class);
			when(history.getBookItem()).thenReturn(bookItem);
			when(history.getUser()).thenReturn(user);
			when(history.getBorrowedAt()).thenReturn(LocalDateTime.now());
			when(history.getReturnedAt()).thenReturn(null);

			given(bookHistoryRepository.findCurrentBorrowsByUserEmail(
				eq("test@bookiki.com"),
				isNull()
			)).willReturn(List.of(history));

			// when
			List<BookHistoryResponse> result = bookHistoryService.getCurrentBorrowedBooks(authUser, null);

			// then
			assertThat(result).hasSize(1);
			verify(bookHistoryRepository).findCurrentBorrowsByUserEmail(
				eq("test@bookiki.com"),
				isNull()
			);
		}

		@Test
		@DisplayName("인증되지 않은 사용자의 경우 UserException 발생")
		void getCurrentBorrowedBooks_Unauthorized() {
			// given
			AuthUser authUser = null;

			// when & then
			assertThatThrownBy(() ->
				bookHistoryService.getCurrentBorrowedBooks(authUser, null)
			).isInstanceOf(UserException.class);
		}

		@Test
		@DisplayName("대출 목록 조회 중 오류 발생 시 BookHistoryException 발생")
		void getCurrentBorrowedBooks_RepositoryError() {
			// given
			AuthUser authUser = mock(AuthUser.class);
			when(authUser.getEmail()).thenReturn("test@bookiki.com");

			given(bookHistoryRepository.findCurrentBorrowsByUserEmail(
				eq("test@bookiki.com"),
				isNull()
			)).willThrow(new RuntimeException("Database error"));

			// when & then
			assertThatThrownBy(() ->
				bookHistoryService.getCurrentBorrowedBooks(authUser, null)
			).isInstanceOf(BookHistoryException.class);
		}
	}

	@Nested
	@DisplayName("연체 상태 확인 테스트")
	class IsOverdue {
		@Test
		@DisplayName("특정 대출 기록의 연체 상태 확인")
		void isOverdue_Success() {
			// given
			Integer bookHistoryId = 1;
			BookHistoryEntity history = mock(BookHistoryEntity.class);
			when(history.getOverdue()).thenReturn(true);

			given(bookHistoryRepository.findById(bookHistoryId))
				.willReturn(Optional.of(history));

			// when
			boolean result = bookHistoryService.isOverdue(bookHistoryId);

			// then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("존재하지 않는 대출 기록 조회 시 예외 발생")
		void isOverdue_NotFound() {
			// given
			Integer bookHistoryId = 1;

			given(bookHistoryRepository.findById(bookHistoryId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() ->
				bookHistoryService.isOverdue(bookHistoryId)
			).isInstanceOf(BookHistoryException.class);
		}
	}
}