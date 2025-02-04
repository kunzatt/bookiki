package com.corp.bookiki.bookhistory.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.dto.BookHistoryRequest;
import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookHistoryService {
	private final BookHistoryRepository bookHistoryRepository;

	public Page<BookHistoryResponse> getBookHistories(BookHistoryRequest request, Pageable pageable) {
		request.validate();

		try {
			LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
			LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

			return bookHistoryRepository.findByBorrowedAtBetween(
				startDateTime,
				endDateTime,
				pageable
			).map(BookHistoryResponse::from);
		} catch (Exception e) {
			log.error("도서 대출 기록 조회 중 오류 발생", e);
			throw new BusinessException(ErrorCode.HISTORY_NOT_FOUND);
		}
	}
}