package com.corp.bookiki.bookitem.repository;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}