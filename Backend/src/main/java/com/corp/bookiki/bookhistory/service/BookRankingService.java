package com.corp.bookiki.bookhistory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;

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

		log.info("Date Range - Start: {}, End: {}", startDate, endDate);

		List<BookItemEntity> bookItems = bookHistoryRepository.findBorrowedBooksFromBookItems(
			startDate,
			endDate
		);

		log.info("Total Book Items found: {}", bookItems.size());
		
		// 각 도서의 대출 이력 로깅
		bookItems.forEach(bookItem -> {
			log.info("Book: {}, Total histories: {}", 
				bookItem.getBookInformation().getTitle(),
				bookItem.getBookHistories().size());
		});

		List<BookRankingResponse> result = bookItems.stream()
			.map(bookItem -> {
				int count = (int) bookItem.getBookHistories().stream()
					.filter(bh -> {
						LocalDateTime borrowedAt = bh.getBorrowedAt();
						boolean isInRange = !borrowedAt.isBefore(startDate) 
							&& !borrowedAt.isAfter(endDate);
						
						log.info("Book: {}, BorrowedAt: {}, InRange: {}", 
							bookItem.getBookInformation().getTitle(), 
							borrowedAt,
							isInRange);
						return isInRange;
					})
					.count();
				
				BookRankingResponse response = new BookRankingResponse(
					bookItem.getId(),
					bookItem.getBookInformation().getTitle(),
					bookItem.getBookInformation().getAuthor(),
					bookItem.getBookInformation().getCategory(),
					bookItem.getBookInformation().getImage(),
					count
				);
				
				log.info("Created response - Book: {}, Count: {}", 
					response.getTitle(), 
					response.getBorrowCount());
				return response;
			})
			.filter(book -> {
				boolean hasCount = book.getBorrowCount() > 0;
				log.info("Filtering - Book: {}, Count: {}, Included: {}", 
					book.getTitle(), 
					book.getBorrowCount(), 
					hasCount);
				return hasCount;
			})
			.sorted(Comparator.comparing(BookRankingResponse::getBorrowCount).reversed())
			.limit(10)
			.collect(Collectors.toList());
		
		log.info("Final result size: {}", result.size());
		return result;
	}

	private BookRankingResponse convertToBookRankingResponse(
		BookItemEntity bookItem,
		LocalDateTime startDate,
		LocalDateTime endDate
	) {
		long borrowCount = bookItem.getBookHistories().stream()
			.filter(bh -> bh.getBorrowedAt().isAfter(startDate) && bh.getBorrowedAt().isBefore(endDate))
			.count();

		return new BookRankingResponse(
			bookItem.getId(),
			bookItem.getBookInformation().getTitle(),
			bookItem.getBookInformation().getAuthor(),
			bookItem.getBookInformation().getCategory(),
			bookItem.getBookInformation().getImage(),
			(int) borrowCount
		);
	}

}
