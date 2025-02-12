package com.corp.bookiki.bookhistory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookRankingService {

	private final BookHistoryRepository bookHistoryRepository;

	@Transactional(readOnly = true)
	public List<BookRankingResponse> getBookRanking() {
		LocalDateTime endDate = LocalDateTime.now();
		LocalDateTime startDate = endDate.minusMonths(1);

		return Optional.of(bookHistoryRepository.findTop10BorrowedBooksFromBookItems(
				startDate,
				endDate,
				null,
				Limit.of(10)
			))
			.filter(list -> !list.isEmpty())
			.orElseThrow(() -> new BookHistoryException(ErrorCode.NO_RANKING_DATA));
	}
}
