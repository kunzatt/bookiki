package com.corp.bookiki.notice.repository;

import com.corp.bookiki.notice.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {
    // findAll(), findById(), save(), delete()는 JpaRepository에서 기본 제공

    // 삭제되지 않은 공지사항만 조회
    List<NoticeEntity> findByDeletedFalseOrderByCreatedAtDesc();

    // 제목, 본문으로 검색 (삭제되지 않은 것만)
    List<NoticeEntity> findByDeletedFalseAndTitleContainingOrContentContainingOrderByCreatedAtDesc(String title, String content);
}
