package com.corp.bookiki.bookhistory.bookborrow;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.corp.bookiki.bookhistory.dto.BookBorrowResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookhistory.service.BookBorrowService;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.loanpolicy.dto.LoanPolicyResponse;
import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;
import com.corp.bookiki.loanpolicy.service.LoanPolicyService;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BookBorrowServiceTest {

	@InjectMocks
	private BookBorrowService bookBorrowService;

	@Mock
	private BookHistoryRepository bookHistoryRepository;

	@Mock
	private BookItemRepository bookItemRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BookHistoryService bookHistoryService;

	@Nested
	@DisplayName("도서 대출 서비스 테스트")
	class BorrowBook {

		@Test
		@DisplayName("정상적인 도서 대출 요청 시 성공")
		void borrowBook_WhenValidRequest_ThenSuccess() {
			// given
			Integer userId = 100;
			Integer bookItemId = 200;

			UserEntity user = mock(UserEntity.class);
			when(user.getId()).thenReturn(userId);
			when(user.getActiveAt()).thenReturn(LocalDateTime.now().minusDays(1)); // 활성 상태

			BookItemEntity bookItem = mock(BookItemEntity.class);
			when(bookItem.getId()).thenReturn(bookItemId);
			when(bookItem.isAvailable()).thenReturn(true);

			BookHistoryEntity history = mock(BookHistoryEntity.class);
			when(history.getId()).thenReturn(1);
			when(history.getBookItem()).thenReturn(bookItem);
			when(history.getUser()).thenReturn(user);

			given(bookItemRepository.findById(bookItemId))
				.willReturn(Optional.of(bookItem));
			given(userRepository.findById(userId))
				.willReturn(Optional.of(user));
			given(bookHistoryRepository.save(any(BookHistoryEntity.class)))
				.willReturn(history);

			// 대출 가능한 책의 수 mocking
			given(bookHistoryService.countCanBorrowBook(userId))
				.willReturn(5);  // 5권 대출 가능

			// when
			BookBorrowResponse response = bookBorrowService.borrowBook(userId, bookItemId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.getBookItemId()).isEqualTo(bookItemId);
			assertThat(response.getUserId()).isEqualTo(userId);
			verify(bookHistoryRepository).save(any(BookHistoryEntity.class));
			verify(bookItem).borrow();
		}
	}
}