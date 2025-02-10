package com.corp.bookiki.bookhistory.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.global.error.exception.LoanPolicyException;
import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;
import com.corp.bookiki.loanpolicy.repository.LoanPolicyRepository;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OverdueService {
	private final BookHistoryRepository bookHistoryRepository;
	private final UserRepository userRepository;
	private final LoanPolicyRepository loanPolicyRepository;

	public void processOverdueBooks() {
		LoanPolicyEntity loanPolicy = loanPolicyRepository.findById(1)
			.orElseThrow(() -> new LoanPolicyException(ErrorCode.LOAN_POLICY_NOT_FOUND));

		LocalDateTime overdueDate = LocalDateTime.now().minusDays(loanPolicy.getLoanPeriod());
		List<BookHistoryEntity> overdueHistories = bookHistoryRepository.findOverdueBooks(overdueDate);

		for (BookHistoryEntity history : overdueHistories) {
			processOverdueBook(history, loanPolicy);
		}
	}

	private void processOverdueBook(BookHistoryEntity history, LoanPolicyEntity loanPolicy) {
		try {
			history.updateOverdueStatus(true);
			bookHistoryRepository.save(history);

			UserEntity user = history.getUser();
			LocalDateTime overdueDate = calculateOverdueDate(history, loanPolicy);
			updateUserActiveAt(user, overdueDate);

			log.info("도서 연체 처리 완료 - 도서ID: {}, 사용자ID: {}, 연체일: {}",
				history.getBookItem().getId(),
				user.getId(),
				overdueDate);
		} catch (Exception e) {
			log.error("도서 연체 처리 중 오류 발생 - 도서ID: {}, 상세 오류: {}",
				history.getBookItem().getId(),
				e.getMessage(),
				e);
			throw new BookHistoryException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private LocalDateTime calculateOverdueDate(BookHistoryEntity history, LoanPolicyEntity loanPolicy) {
		LocalDateTime expectedReturnDate = history.getBorrowedAt().plusDays(loanPolicy.getLoanPeriod());

		if (history.getReturnedAt() != null) {
			long overdueDays = ChronoUnit.DAYS.between(expectedReturnDate, history.getReturnedAt());
			return history.getReturnedAt().plusDays(overdueDays);
		} else {
			long overdueDays = ChronoUnit.DAYS.between(expectedReturnDate, LocalDateTime.now());
			return LocalDateTime.now().plusDays(overdueDays);
		}
	}

	private void updateUserActiveAt(UserEntity user, LocalDateTime overdueDate) {
		user.updateActiveAt(overdueDate);
		userRepository.save(user);
	}
}