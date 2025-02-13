package com.corp.bookiki.bookhistory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookRankingService {

	private final BookHistoryRepository bookHistoryRepository;

	@Transactional(readOnly = true)
	public List<BookRankingResponse> getBookRanking() {
		LocalDateTime endDate = LocalDateTime.now();
		LocalDateTime startDate = endDate.minusMonths(1);

		log.info("Getting book ranking for period - Start: {}, End: {}", startDate, endDate);

		List<Object[]> rankings = bookHistoryRepository.findTopMostBorrowedBooks(startDate, endDate);

		return rankings.stream()
			.map(result -> new BookRankingResponse(
				(Integer) result[0],
				(String) result[1],
				(String) result[2],
				(Integer) result[3],
				(String) result[4],
				(Long) result[5]
			))
			.collect(Collectors.toList());
	}
}