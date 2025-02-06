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

import com.corp.bookiki.bookhistory.dto.BookBorrowRequest;
import com.corp.bookiki.bookhistory.dto.BookBorrowResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookhistory.service.BookBorrowService;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
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

	@Nested
	@DisplayName("도서 대출 서비스 테스트")
	class BorrowBook {

		@Test
		@DisplayName("정상적인 도서 대출 요청 시 성공")
		void borrowBook_WhenValidRequest_ThenSuccess() {
			// given
			BookBorrowRequest request = new BookBorrowRequest();
			request.setUserId(100);
			request.setBookItemId(200);

			UserEntity user = mock(UserEntity.class);
			when(user.getId()).thenReturn(request.getUserId());

			BookItemEntity bookItem = mock(BookItemEntity.class);
			when(bookItem.getId()).thenReturn(request.getBookItemId());
			when(bookItem.isAvailable()).thenReturn(true);

			BookHistoryEntity history = mock(BookHistoryEntity.class);
			when(history.getId()).thenReturn(1);
			when(history.getBookItem()).thenReturn(bookItem);
			when(history.getUser()).thenReturn(user);

			given(bookItemRepository.findById(request.getBookItemId()))
				.willReturn(Optional.of(bookItem));
			given(userRepository.findById(request.getUserId()))
				.willReturn(Optional.of(user));
			given(bookHistoryRepository.save(any(BookHistoryEntity.class)))
				.willReturn(history);

			LocalDateTime before = LocalDateTime.now().minusDays(1);
			when(user.getActiveAt()).thenReturn(before);

			log.info("테스트 데이터 설정 완료: userId={}, bookItemId={}",
				request.getUserId(), request.getBookItemId());

			// when
			BookBorrowResponse response = bookBorrowService.borrowBook(request);
			log.info("도서 대출 처리 완료: historyId={}", response.getId());

			// then
			assertThat(response).isNotNull();
			assertThat(response.getBookItemId()).isEqualTo(request.getBookItemId());
			assertThat(response.getUserId()).isEqualTo(request.getUserId());
			verify(bookHistoryRepository).save(any(BookHistoryEntity.class));
			verify(bookItem).borrow();
			log.info("도서 대출 성공 검증 완료");
		}
	}
}