package com.corp.bookiki.bookhistory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;
import com.corp.bookiki.loanpolicy.service.LoanPolicyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookHistoryService {
	private final BookHistoryRepository bookHistoryRepository;
	private final LoanPolicyService loanPolicyService;

	public Page<BookHistoryResponse> getAdminBookHistories(
		LocalDate startDate,
		LocalDate endDate,
		String userName,
		String companyId,
		Boolean overdue,
		Pageable pageable
	) {
		try {
			log.debug("관리자용 대출 기록 조회 시작 - 시작일: {}, 종료일: {}, 사용자명: {}, 회사ID: {}, 연체여부: {}",
				startDate, endDate, userName, companyId, overdue);

			LocalDateTime startDateTime = startDate.atStartOfDay();
			LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

			Page<BookHistoryEntity> histories = bookHistoryRepository.findAllBookHistoriesForAdmin(
				startDateTime, endDateTime, userName, companyId, overdue, pageable);

			log.debug("관리자용 대출 기록 조회 완료 - 총 {} 건", histories.getTotalElements());

			return histories.map(BookHistoryResponse::from);
		} catch (Exception e) {
			log.error("관리자용 대출 기록 조회 중 오류 발생", e);
			throw new BookHistoryException(ErrorCode.HISTORY_NOT_FOUND);
		}
	}

	public List<BookHistoryResponse> getCurrentBorrowedBooks(Integer userId, Boolean overdue) {
		try {
			log.debug("현재 대출 중인 도서 조회 시작 - 사용자ID: {}, 연체만 조회: {}", userId, overdue);

			List<BookHistoryEntity> currentBorrows = bookHistoryRepository.findCurrentBorrowsByUserId(userId, overdue);

			log.debug("현재 대출 중인 도서 조회 완료 - 사용자ID: {}, 총 {} 건", userId, currentBorrows.size());

			return currentBorrows.stream().map(BookHistoryResponse::from).toList();
		} catch (Exception e) {
			log.error("현재 대출 중인 도서 조회 중 오류 발생 - 사용자ID: {}", userId, e);
			throw new BookHistoryException(ErrorCode.HISTORY_NOT_FOUND);
		}
	}

	public Page<BookHistoryResponse> getUserBookHistories(
		Integer userId,
		LocalDate startDate,
		LocalDate endDate,
		Boolean overdue,
		Pageable pageable
	) {
		try {
			log.debug("사용자 대출 기록 조회 시작 - 사용자ID: {}, 시작일: {}, 종료일: {}, 연체여부: {}",
				userId, startDate, endDate, overdue);

			LocalDateTime startDateTime = startDate.atStartOfDay();
			LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

			Page<BookHistoryEntity> histories = bookHistoryRepository.findAllForUser(
				userId, startDateTime, endDateTime, overdue, pageable);

			log.debug("사용자 대출 기록 조회 완료 - 총 {} 건", histories.getTotalElements());

			return histories.map(BookHistoryResponse::from);
		} catch (Exception e) {
			log.error("사용자 대출 기록 조회 중 오류 발생 - 사용자ID: {}", userId, e);
			throw new BookHistoryException(ErrorCode.HISTORY_NOT_FOUND);
		}
	}

	public List<BookHistoryEntity> checkOneDayBeforeReturn() {
		Integer loanPeriod = loanPolicyService.getCurrentPolicy().getLoanPeriod();

		// 현재 날짜로부터 (대출기간-2)일 전에 빌린 책들을 찾음
		LocalDateTime targetDate = LocalDateTime.now()
			.minusDays(loanPeriod - 2)
			.with(LocalTime.MIN); // 해당 날짜의 시작시간으로 설정

		return bookHistoryRepository.findByBorrowedAtAndNotReturned(targetDate);
	}

	public boolean isBookCurrentlyBorrowed(BookItemEntity bookItem) {
		return bookHistoryRepository.findCurrentBorrowByBookItem(bookItem).isPresent();
	}

	public boolean isOverdue(Integer bookHistoryId) {
		return bookHistoryRepository.findById(bookHistoryId)
			.map(BookHistoryEntity::getOverdue)
			.orElseThrow(() -> new BookHistoryException(ErrorCode.HISTORY_NOT_FOUND));
	}

	public Integer countCanBorrowBook(Integer userId) {
		Integer count = bookHistoryRepository.countByUserIdAndReturnedAtIsNull(userId);
		if(count == null) count = 0;
		Integer maxBooks = loanPolicyService.getCurrentPolicy().getMaxBooks();
		return maxBooks-count;
	}
}
