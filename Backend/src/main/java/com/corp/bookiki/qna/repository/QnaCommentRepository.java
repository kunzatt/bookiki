package com.corp.bookiki.qna.repository;

import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaCommentRepository extends JpaRepository<QnaCommentEntity, Integer> {
    // 특정 문의사항에 달린 삭제되지 않은 댓글을 생성일시 기준 오름차순으로 조회
    List<QnaCommentEntity> findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(int qnaId);

    // 특정 문의사항의 모든 답변 삭제 
    void deleteByQnaId(int qnaId);

    int qna(QnaEntity qna);
}
