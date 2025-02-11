package com.corp.bookiki.qna.repository;

import com.corp.bookiki.qna.entity.QnaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QnaRepository extends JpaRepository<QnaEntity, Integer> {
    @Query("SELECT q FROM QnaEntity q " +
            "WHERE q.deleted = false " +
            "AND (:authorId IS NULL OR q.user.id = :authorId) " +
            "AND (:qnaType IS NULL OR q.qnaType = :qnaType) " +
            "AND (:keyword IS NULL OR q.title LIKE %:keyword% OR q.content LIKE %:keyword%) " +
            "ORDER BY q.createdAt DESC")
    Page<QnaEntity> findBySearchCriteria(
            @Param("authorId") Integer authorId,
            @Param("qnaType") String qnaType,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
