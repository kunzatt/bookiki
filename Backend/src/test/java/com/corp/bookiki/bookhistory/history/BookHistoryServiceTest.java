package com.corp.bookiki.bookhistory.history;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

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

import com.corp.bookiki.bookhistory.dto.BookHistoryRequest;
import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.enitity.PeriodType;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.global.error.exception.BusinessException;
import com.corp.bookiki.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class BookHistoryServiceTest {
	@InjectMocks
	private BookHistoryService bookHistoryService;

	@Mock
	private BookHistoryRepository bookHistoryRepository;

	@Mock
	private BookItemEntity bookItem;

	@Mock
	private UserEntity user;

	@Nested
	@DisplayName("도서 대출 기록 조회 테스트")
	class GetBookHistories {
		@Test
		@DisplayName("LAST_WEEK 기간으로 조회 시 성공")
		void getBookHistories_WhenLastWeek_ThenSuccess() {
			// given
			BookHistoryRequest request = new BookHistoryRequest();
			request.setPeriodType(PeriodType.LAST_WEEK);
			request.setStartDate(LocalDate.now().minusDays(7));
			request.setEndDate(LocalDate.now());

			given(bookItem.getId()).willReturn(1);
			given(user.getId()).willReturn(1);

			BookHistoryEntity entity = new BookHistoryEntity(bookItem, user);
			Page<BookHistoryEntity> page = new PageImpl<>(List.of(entity));

			given(bookHistoryRepository.findByBorrowedAtBetween(request.getStartDate().atStartOfDay(),
				request.getEndDate().atTime(23, 59, 59), PageRequest.of(0, 10))).willReturn(page);

			// when
			Page<BookHistoryResponse> result = bookHistoryService.getBookHistories(request, PageRequest.of(0, 10));

			// then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).getBookItemId()).isEqualTo(1);

			verify(bookHistoryRepository).findByBorrowedAtBetween(request.getStartDate().atStartOfDay(),
				request.getEndDate().atTime(23, 59, 59), PageRequest.of(0, 10));
		}

		@Test
		@DisplayName("CUSTOM 기간으로 조회 시 성공")
		void getBookHistories_WhenCustomPeriod_ThenSuccess() {
			// given
			BookHistoryRequest request = new BookHistoryRequest();
			request.setPeriodType(PeriodType.CUSTOM);
			request.setStartDate(LocalDate.now().minusDays(7));
			request.setEndDate(LocalDate.now());

			given(bookItem.getId()).willReturn(1);
			given(user.getId()).willReturn(1);

			BookHistoryEntity entity = new BookHistoryEntity(bookItem, user);
			Page<BookHistoryEntity> page = new PageImpl<>(List.of(entity));

			given(bookHistoryRepository.findByBorrowedAtBetween(request.getStartDate().atStartOfDay(),
				request.getEndDate().atTime(23, 59, 59), PageRequest.of(0, 10))).willReturn(page);

			// when
			Page<BookHistoryResponse> result = bookHistoryService.getBookHistories(request, PageRequest.of(0, 10));

			// then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).getBookItemId()).isEqualTo(1);

			verify(bookHistoryRepository).findByBorrowedAtBetween(request.getStartDate().atStartOfDay(),
				request.getEndDate().atTime(23, 59, 59), PageRequest.of(0, 10));
		}

		@Test
		@DisplayName("validate 실패 시 예외 발생")
		void getBookHistories_WhenValidateFails_ThenThrowException() {
			// given
			BookHistoryRequest request = new BookHistoryRequest();
			request.setPeriodType(PeriodType.CUSTOM);
			// startDate와 endDate를 설정하지 않음

			// when & then
			assertThrows(BusinessException.class,
				() -> bookHistoryService.getBookHistories(request, PageRequest.of(0, 10)));
		}
	}
}