package com.corp.bookiki.qna.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaException;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaDetailResponse;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.dto.QnaUpdate;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QnaService {
    // 개발용 임시 상수
    private static final int TEST_AUTHOR_ID = 1;
    private static final String TEST_AUTHOR_NAME = "박성문";

    private final QnaRepository qnaRepository;
    private final QnaCommentService qnaCommentService; // 문의사항 상제 조회에서 댓글 목록을 불러오기 위함

    // 문의사항 등록
    @Transactional
    public int creatQna(QnaRequest request, int authorId) {
        // user 관련 서비스 완성 후 수정
        QnaEntity qna = request.toEntity(TEST_AUTHOR_ID);
        qnaRepository.save(qna);
        return qna.getId();
    }

    // 문의사항 전체 조회 (정렬, 검색 포함)
    @Transactional(readOnly = true)
    public List<QnaEntity> selectAllQnas(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return qnaRepository.findByDeletedFalseOrderByCreatedAtDesc();
        }
        return qnaRepository.findByDeletedFalseAndTitleContainingOrContentContainingOrderByCreatedAtDesc(keyword, keyword);
    }

    // 문의사항 1개 조회
    @Transactional(readOnly = true)
    public QnaEntity selectQnaById(int id) {
        QnaEntity qna = qnaRepository.getReferenceById(id);
        if (qna == null || qna.isDeleted()) {
            throw new QnaException(ErrorCode.QNA_NOT_FOUND);
        }
        return qna;
    }

    // 댓글 목록까지 포함한 상세 조회 메서드
    @Transactional(readOnly = true)
    public QnaDetailResponse selectQnaByIdWithComment(int id) {
        QnaEntity qna = selectQnaById(id);
        List<QnaCommentResponse> comments = qnaCommentService.selectCommentsByQnaId(id);
        return new QnaDetailResponse(qna, TEST_AUTHOR_NAME, comments);
    }

    // 문의사항 삭제
    @Transactional
    public void deleteQna(int id) {
        QnaEntity qna = selectQnaById(id);
        qna.delete();
    }

    // 문의사항 수정
    @Transactional
    public void updateQna(QnaUpdate update) {
        QnaEntity qna = selectQnaById(update.getId());
        qna.update(update.getTitle(), update.getContent());
    }
}
