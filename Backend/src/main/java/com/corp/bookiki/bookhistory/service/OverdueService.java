package com.corp.bookiki.bookhistory.service;

import java.time.LocalDateTime;
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
		try {
			log.debug("연체 도서 처리 시작");

			List<BookHistoryEntity> overdueHistories = bookHistoryRepository.findOverdueBooks();
			log.debug("처리할 연체 도서 수: {}", overdueHistories.size());

			LoanPolicyEntity loanPolicy = loanPolicyRepository.findFirstByOrderByIdDesc()
				.orElseThrow(() -> new LoanPolicyException(ErrorCode.LOAN_POLICY_NOT_FOUND));

			for (BookHistoryEntity history : overdueHistories) {
				processOverdueBook(history, loanPolicy);
			}

			log.debug("연체 도서 처리 완료");
		} catch (Exception e) {
			log.error("연체 도서 처리 중 오류 발생", e);
			throw new BookHistoryException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private void processOverdueBook(BookHistoryEntity history, LoanPolicyEntity loanPolicy) {
		try {
			log.debug("도서 연체 처리 시작 - 도서ID: {}, 사용자ID: {}",
				history.getBookItem().getId(), history.getUser().getId());

			if (history.checkOverdue(loanPolicy)) {
				LocalDateTime overdueDate = calculateOverdueDate(history, loanPolicy);
				updateUserActiveAt(history.getUser(), overdueDate);
				history.updateOverdueStatus(true);
				bookHistoryRepository.save(history);

				log.debug("도서 연체 처리 완료 - 도서ID: {}, 사용자ID: {}, 연체일: {}",
					history.getBookItem().getId(), history.getUser().getId(), overdueDate);
			}
		} catch (Exception e) {
			log.error("도서 연체 처리 중 오류 발생 - 도서ID: {}", history.getBookItem().getId(), e);
			throw new BookHistoryException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private LocalDateTime calculateOverdueDate(BookHistoryEntity history, LoanPolicyEntity loanPolicy) {
		return history.getBorrowedAt().plusDays(loanPolicy.getLoanPeriod());
	}

	private void updateUserActiveAt(UserEntity user, LocalDateTime overdueDate) {
		user.updateActiveAt(overdueDate);
		userRepository.save(user);
	}
}