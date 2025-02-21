package com.corp.bookiki.qna.repository;

import com.corp.bookiki.qna.entity.QnaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QnaRepository extends JpaRepository<QnaEntity, Integer> {
    @Query("SELECT DISTINCT q FROM QnaEntity q " +
            "WHERE q.deleted = false " +
            "AND (:authorId IS NULL OR q.user.id = :authorId) " +
            "AND (:qnaType IS NULL OR q.qnaType = :qnaType) " +
            "AND (:keyword IS NULL OR q.title LIKE %:keyword% OR q.content LIKE %:keyword%) " +
            "AND (:answered IS NULL OR " +
            "   CASE WHEN :answered = true THEN " +
            "       EXISTS (SELECT 1 FROM q.comments c WHERE CAST(c.deleted AS boolean) = false) " +
            "   ELSE " +
            "       NOT EXISTS (SELECT 1 FROM q.comments c WHERE CAST(c.deleted AS boolean) = false) " +
            "   END)")
    Page<QnaEntity> findBySearchCriteria(
            @Param("authorId") Integer authorId,
            @Param("qnaType") String qnaType,
            @Param("keyword") String keyword,
            @Param("answered") Boolean answered,
            Pageable pageable
    );
}
