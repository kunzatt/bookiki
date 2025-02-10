package com.corp.bookiki.notice.repository;

import com.corp.bookiki.notice.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {
    // 삭제되지 않은 공지사항만 조회 (페이지네이션 적용)
    Page<NoticeEntity> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // 제목, 본문으로 검색 (삭제되지 않은 것만, 페이지네이션 적용)
    @Query("SELECT n FROM NoticeEntity n " +
            "WHERE n.deleted = false " +
            "AND (n.title LIKE CONCAT('%', :keyword, '%') OR n.content LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY n.createdAt DESC")
    Page<NoticeEntity> findBySearchCriteria(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
