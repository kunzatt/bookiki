package com.corp.bookiki.bookhistory.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;

@Repository
public interface BookHistoryRepository extends JpaRepository<BookHistoryEntity, Integer> {

	List<BookHistoryResponse> findByBorrowedAtBetween(LocalDateTime borrowedAtAfter, LocalDateTime borrowedAtBefore, Limit limit);
	Page<BookHistoryEntity> findByBorrowedAtBetween(
		LocalDateTime startDate,
		LocalDateTime endDate,
		Pageable pageable
	);
}
