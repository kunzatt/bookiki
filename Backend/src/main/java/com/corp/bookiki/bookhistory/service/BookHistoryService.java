package com.corp.bookiki.bookhistory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookHistoryService {
	private final BookHistoryRepository bookHistoryRepository;

	public Page<BookHistoryResponse> getBookHistories(
		LocalDate startDate,
		LocalDate endDate,
		String keyword,
		Pageable pageable
	) {
		try {
			log.debug("대출 기록 조회 시작 - 시작일: {}, 종료일: {}, 키워드: {}", startDate, endDate, keyword);

			LocalDateTime startDateTime = startDate.atStartOfDay();
			LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

			Page<BookHistoryEntity> histories = bookHistoryRepository
				.searchBookHistoryWithCount(startDateTime, endDateTime, keyword, pageable);

			log.debug("대출 기록 조회 완료 - 총 {} 건", histories.getTotalElements());

			return histories.map(BookHistoryResponse::from);
		} catch (Exception e) {
			log.error("도서 대출 기록 조회 중 오류 발생 - 시작일: {}, 종료일: {}, 키워드: {}",
				startDate, endDate, keyword, e);
			throw new BookHistoryException(ErrorCode.HISTORY_NOT_FOUND);
		}
	}

	public List<BookHistoryResponse> getCurrentBorrowsByUser(UserEntity user) {
		try {
			log.debug("사용자 현재 대출 목록 조회 - 사용자 ID: {}", user.getId());
			return bookHistoryRepository.findCurrentBorrowsByUser(user)
				.stream()
				.map(BookHistoryResponse::from)
				.toList();
		} catch (Exception e) {
			log.error("사용자 대출 목록 조회 중 오류 발생 - 사용자 ID: {}", user.getId(), e);
			throw new BookHistoryException(ErrorCode.HISTORY_NOT_FOUND);
		}
	}

	public Page<BookHistoryResponse> getUserBookHistories(
		Integer userId,
		LocalDate startDate,
		LocalDate endDate,
		String keyword,
		Pageable pageable
	) {
		try {
			log.debug("사용자별 대출 기록 조회 시작 - 사용자: {}, 시작일: {}, 종료일: {}, 키워드: {}",
				userId, startDate, endDate, keyword);

			LocalDateTime startDateTime = startDate.atStartOfDay();
			LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

			Page<BookHistoryEntity> histories = bookHistoryRepository
				.searchUserBookHistoryWithCount(userId, startDateTime, endDateTime, keyword, pageable);

			log.debug("사용자별 대출 기록 조회 완료 - 사용자: {}, 총 {} 건",
				userId, histories.getTotalElements());

			return histories.map(BookHistoryResponse::from);
		} catch (Exception e) {
			log.error("사용자별 도서 대출 기록 조회 중 오류 발생 - 사용자: {}, 시작일: {}, 종료일: {}, 키워드: {}",
				userId, startDate, endDate, keyword, e);
			throw new BookHistoryException(ErrorCode.HISTORY_NOT_FOUND);
		}
	}


	public boolean isBookCurrentlyBorrowed(BookItemEntity bookItem) {
		return bookHistoryRepository.findCurrentBorrowByBookItem(bookItem).isPresent();
	}
}