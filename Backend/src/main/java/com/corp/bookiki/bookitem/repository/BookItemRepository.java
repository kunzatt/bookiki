package com.corp.bookiki.bookitem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;

import java.util.List;

@Repository
public interface BookItemRepository extends JpaRepository<BookItemEntity, Integer> {
	Page<BookItemEntity> findByDeletedFalse(Pageable pageable);

	@Query(value = """
    SELECT item FROM BookItemEntity item 
    JOIN FETCH item.bookInformation info 
    WHERE item.deleted = false 
    AND (:keyword IS NULL OR LOWER(info.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
    OR LOWER(info.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """,
	countQuery = """
    SELECT COUNT(item) FROM BookItemEntity item 
    JOIN item.bookInformation info 
    WHERE item.deleted = false 
    AND (:keyword IS NULL OR LOWER(info.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
    OR LOWER(info.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
	Page<BookItemEntity> findAllWithKeyword(
			@Param("keyword") String keyword,
			Pageable pageable
	);

	@Query(value = """
    SELECT item FROM BookItemEntity item 
    JOIN FETCH item.bookInformation info 
    WHERE item.deleted = false 
    AND (:type = 'TITLE' AND LOWER(info.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR :type = 'AUTHOR' AND LOWER(info.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR :type = 'PUBLISHER' AND LOWER(info.publisher) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR :type = 'KEYWORD' AND (LOWER(info.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
    OR LOWER(info.description) LIKE LOWER(CONCAT('%', :keyword, '%'))))
    """)
	Page<BookItemEntity> searchBooks(
			@Param("type") String type,
			@Param("keyword") String keyword,
			Pageable pageable
	);

	int countByBookStatus(BookStatus bookStatus);

	// 인기 도서 조회 (대출 횟수 기준)
	@Query("SELECT bi FROM BookItemEntity bi " +
			"LEFT JOIN BookHistoryEntity bh ON bi.id = bh.bookItem.id " +
			"WHERE bi.bookStatus = :status " +
			"AND (:category IS NULL OR bi.bookInformation.category = :category) " +
			"AND bi.deleted = false " +
			"GROUP BY bi.id " +
			"ORDER BY COUNT(bh.id) DESC")
	List<BookItemEntity> findPopularBooks(
			@Param("category") Integer category,
			@Param("status") BookStatus status,
			Pageable pageable
	);
}