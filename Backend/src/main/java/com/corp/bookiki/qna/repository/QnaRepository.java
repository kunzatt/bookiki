package com.corp.bookiki.qna.repository;

import com.corp.bookiki.qna.entity.QnaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaRepository extends JpaRepository<QnaEntity, Integer> {
    // 삭제되지 않은 문의사항을 생성일시 기준 내림차순으로 조회
    List<QnaEntity> findByDeletedFalseOrderByCreatedAtDesc();

    // 삭제되지 않은 문의사항 중 제목이나 내용에 검색어가 포함된 항목을 생성일시 기준 내림차순으로 조회
    List<QnaEntity> findByDeletedFalseAndTitleContainingOrContentContainingOrderByCreatedAtDesc(String title, String content);

    // 특정 유형의 문의사항만 조회
    List<QnaEntity> findByDeletedFalseAndQnaTypeOrderByCreatedAtDesc(String qnaType);

    // 해당 사용자에 대한 문의사항만 조회
    List<QnaEntity> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(int authorId);
}
