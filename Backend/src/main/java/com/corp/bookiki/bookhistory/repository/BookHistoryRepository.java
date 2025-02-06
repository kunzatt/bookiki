package com.corp.bookiki.bookhistory.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.user.entity.UserEntity;

public interface BookHistoryRepository extends JpaRepository<BookHistoryEntity, Integer> {

	// 날짜 범위로 대출 기록 검색 (카운트 쿼리 최적화)
	@Query(value = """
		SELECT bh 
		FROM BookHistoryEntity bh 
		JOIN FETCH bh.bookItem bi 
		JOIN FETCH bi.bookInformation bi_info
		JOIN FETCH bh.user u
		WHERE bh.borrowedAt BETWEEN :startDate AND :endDate
		AND (:keyword IS NULL 
		    OR bi_info.title LIKE %:keyword% 
		    OR bi_info.author LIKE %:keyword%)
		""", countQuery = """
		SELECT COUNT(bh) 
		FROM BookHistoryEntity bh 
		JOIN bh.bookItem bi 
		JOIN bi.bookInformation bi_info
		WHERE bh.borrowedAt BETWEEN :startDate AND :endDate
		AND (:keyword IS NULL 
		    OR bi_info.title LIKE %:keyword% 
		    OR bi_info.author LIKE %:keyword%)
		""")
	Page<BookHistoryEntity> searchBookHistoryWithCount(@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate, @Param("keyword") String keyword, Pageable pageable);

	// 특정 도서의 현재 대출 상태 확인
	@Query("""
		SELECT bh
		FROM BookHistoryEntity bh
		WHERE bh.bookItem = :bookItem
		AND bh.returnedAt IS NULL
		""")
	Optional<BookHistoryEntity> findCurrentBorrowByBookItem(@Param("bookItem") BookItemEntity bookItem);

	// 특정 사용자의 현재 대출 목록 조회
	@Query("""
		SELECT bh
		FROM BookHistoryEntity bh
		JOIN FETCH bh.bookItem bi
		JOIN FETCH bi.bookInformation
		WHERE bh.user = :user
		AND bh.returnedAt IS NULL
		""")
	List<BookHistoryEntity> findCurrentBorrowsByUser(@Param("user") UserEntity user);

	// 연체 도서 목록 조회 (반납일이 지났지만 반납되지 않은 도서)
	@Query("""
		SELECT bh
		FROM BookHistoryEntity bh
		JOIN FETCH bh.bookItem bi
		JOIN FETCH bi.bookInformation
		JOIN FETCH bh.user
		WHERE bh.borrowedAt < :overdueDate
		AND bh.returnedAt IS NULL
		""")
	List<BookHistoryEntity> findOverdueBooks(@Param("overdueDate") LocalDateTime overdueDate);

	// 특정 기간 내 가장 많이 대출된 도서 순위
	@Query("""
		SELECT bi.bookInformation.id, COUNT(bh) as borrowCount
		FROM BookHistoryEntity bh
		JOIN bh.bookItem bi
		WHERE bh.borrowedAt BETWEEN :startDate AND :endDate
		GROUP BY bi.bookInformation.id
		ORDER BY borrowCount DESC
		""")
	List<Object[]> findMostBorrowedBooks(@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate, Pageable pageable);

	@Query(value = """
		SELECT bh
		FROM BookHistoryEntity bh
		JOIN FETCH bh.bookItem bi
		JOIN FETCH bi.bookInformation bi_info
		JOIN FETCH bh.user u
		WHERE bh.user.id = :userId
		AND bh.borrowedAt BETWEEN :startDate AND :endDate
		AND (:keyword IS NULL
		    OR bi_info.title LIKE %:keyword%
		    OR bi_info.author LIKE %:keyword%)
		""", countQuery = """
		SELECT COUNT(bh)
		FROM BookHistoryEntity bh
		JOIN bh.bookItem bi
		JOIN bi.bookInformation bi_info
		WHERE bh.user.id = :userId
		AND bh.borrowedAt BETWEEN :startDate AND :endDate
		AND (:keyword IS NULL
		    OR bi_info.title LIKE %:keyword%
		    OR bi_info.author LIKE %:keyword%)
		""")
	Page<BookHistoryEntity> searchUserBookHistoryWithCount(@Param("userId") Integer userId,
		@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate,
		@Param("keyword") String keyword, Pageable pageable);

	@Query("""
		    SELECT bh 
		    FROM BookHistoryEntity bh 
		    JOIN FETCH bh.bookItem bi 
		    JOIN FETCH bi.bookInformation 
		    WHERE bh.borrowedAt BETWEEN :startDateTime AND :endDateTime
		""")
	List<BookHistoryEntity> findByBorrowedAtBetween(@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime, Limit limit);

	@Query(value = """
		SELECT bh 
		FROM BookHistoryEntity bh 
		JOIN FETCH bh.bookItem bi 
		JOIN FETCH bi.bookInformation bi_info
		JOIN FETCH bh.user u
		WHERE bh.borrowedAt BETWEEN :startDate AND :endDate
		AND (:userName IS NULL OR u.userName LIKE %:userName%)
		AND (:companyId IS NULL OR u.companyId = :companyId)
		AND (:isOverdue IS NULL OR bh.overdue = :isOverdue)
		""", countQuery = """
		SELECT COUNT(bh) 
		FROM BookHistoryEntity bh 
		JOIN bh.user u
		WHERE bh.borrowedAt BETWEEN :startDate AND :endDate
		AND (:userName IS NULL OR u.userName LIKE %:userName%)
		AND (:companyId IS NULL OR u.companyId = :companyId)
		AND (:isOverdue IS NULL OR bh.overdue = :isOverdue)
		""")
	Page<BookHistoryEntity> findAllForAdmin(@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate, @Param("userName") String userName,
		@Param("companyId") String companyId, @Param("isOverdue") Boolean isOverdue, Pageable pageable);

	@Query(value = """
		SELECT bh
		FROM BookHistoryEntity bh
		JOIN FETCH bh.bookItem bi
		JOIN FETCH bi.bookInformation bi_info
		JOIN FETCH bh.user u
		WHERE bh.user.email = :userEmail
		AND bh.borrowedAt BETWEEN :startDate AND :endDate
		AND (:overdue IS NULL OR bh.overdue = :overdue)
		""", countQuery = """
		SELECT COUNT(bh)
		FROM BookHistoryEntity bh
		JOIN bh.user u
		WHERE u.email = :userEmail
		AND bh.borrowedAt BETWEEN :startDate AND :endDate
		AND (:isOverdue IS NULL OR bh.overdue = :isOverdue)
		""")
	Page<BookHistoryEntity> findAllForUser(@Param("userEmail") String userEmail,
		@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate,
		@Param("overdue") Boolean overdue, Pageable pageable);

	@Query("""
		SELECT bh
		FROM BookHistoryEntity bh
		JOIN FETCH bh.bookItem bi
		JOIN FETCH bi.bookInformation
		WHERE bh.user.email = :userEmail
		AND bh.returnedAt IS NULL
		AND (:overdue IS NULL OR (:overdue = true AND bh.overdue = true))
		ORDER BY bh.borrowedAt DESC
		""")
	List<BookHistoryEntity> findCurrentBorrowsByUserEmail(@Param("userEmail") String userEmail,
		@Param("overdue") Boolean overdue);

}