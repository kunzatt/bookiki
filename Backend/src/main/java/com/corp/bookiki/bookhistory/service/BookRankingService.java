package com.corp.bookiki.bookhistory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookRankingService {

	private final BookHistoryRepository bookHistoryRepository;
	private final BookItemRepository bookItemRepository;

	@Transactional(readOnly = true)
	public List<BookRankingResponse> getBookRanking() {
		LocalDateTime endDate = LocalDateTime.now();
		LocalDateTime startDate = endDate.minusMonths(1);

		log.info("Getting book ranking for period - Start: {}, End: {}", startDate, endDate);

		List<Object[]> rankings = bookHistoryRepository.findTopMostBorrowedBooks(startDate, endDate);

		return rankings.stream()
			.map(result -> {
				Integer bookItemId = ((Number) result[0]).intValue();
				String title = (String) result[1];
				Long borrowCount = ((Number) result[2]).longValue();

				BookItemEntity bookItem = bookItemRepository.findByIdWithBookInformation(bookItemId)
					.orElseThrow(() -> new BookHistoryException(ErrorCode.BOOK_ITEM_NOT_FOUND));

				return new BookRankingResponse(
					bookItemId,
					title,
					bookItem.getBookInformation().getAuthor(),
					bookItem.getBookInformation().getCategory(),
					bookItem.getBookInformation().getImage(),
					borrowCount
				);
			})
			.collect(Collectors.toList());
	}
}