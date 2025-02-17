package com.corp.bookiki.bookitem.repository;

import java.util.List;
import java.util.Optional;

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

	@Query("SELECT bi FROM BookItemEntity bi " +
		"WHERE bi.bookInformation.id = " +
		"(SELECT b.bookInformation.id FROM BookItemEntity b WHERE b.id = :bookItemId)")
	List<BookItemEntity> findByBookInformationIdFromBookItemId(@Param("bookItemId") Integer bookItemId);

	@Query("SELECT bi.id FROM BookItemEntity bi " +
		"WHERE bi.bookInformation.id = " +
		"(SELECT b.bookInformation.id FROM BookItemEntity b WHERE b.id = :bookItemId)")
	List<Integer> findIdsByBookInformationIdFromBookItemId(@Param("bookItemId") Integer bookItemId);

	List<Integer> findIdsByBookStatus(BookStatus bookStatus);

	@Query("SELECT bi FROM BookItemEntity bi JOIN FETCH bi.bookInformation WHERE bi.id = :id")
	Optional<BookItemEntity> findByIdWithBookInformation(@Param("id") Integer id);

	@Query(value = """
    SELECT item FROM BookItemEntity item 
    JOIN FETCH item.bookInformation info 
    LEFT JOIN FETCH item.qrCode
    WHERE item.deleted = false 
    AND (:keyword IS NULL OR 
        LOWER(info.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(info.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(info.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        info.isbn LIKE CONCAT('%', :keyword, '%'))
    """)
	Page<BookItemEntity> findAllBooksForAdmin(
			@Param("keyword") String keyword,
			Pageable pageable
	);
}