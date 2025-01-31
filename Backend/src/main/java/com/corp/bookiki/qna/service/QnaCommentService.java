package com.corp.bookiki.qna.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaCommentException;
import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QnaCommentService {

    private final QnaCommentRepository qnaCommentRepository;
    private final QnaService qnaService;

    // 문의사항Id 별 답변 리스트 조회
    @Transactional
    public List<QnaCommentResponse> selectQnaCommentsByQnaId(int qnaId) {
        try {
            List<QnaCommentEntity> comments = qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(qnaId);
            List<QnaCommentResponse> responses = new ArrayList<>();

            for (QnaCommentEntity comment : comments) {
                responses.add(new QnaCommentResponse(comment));
            }

            return responses;
        } catch (Exception ex) {
            log.error("QNA 댓글 조회 실패: qnaId={}", qnaId, ex);
            throw new QnaCommentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 1개 조회
    @Transactional(readOnly = true)
    public QnaCommentEntity selectQnaCommentById(int commentId) {
        QnaCommentEntity comment = qnaCommentRepository.getReferenceById(commentId);
        return comment;
    }

    // 문의사항 답변 등록
    @Transactional
    public int createQnaComment(QnaCommentRequest request, int authorId) {
        // 해당 문의사항 존재 확인
        qnaService.selectQnaById(request.getQnaId());

        QnaCommentEntity comment = request.toEntity(authorId);
        qnaCommentRepository.save(comment);
        return comment.getId();
    }

    // 문의사항 답변 삭제
    @Transactional
    public void deleteQnaComment(int commentId) {
        QnaCommentEntity comment = selectQnaCommentById(commentId);
        comment.delete();
    }

    // 문의사항 답변 수정
    @Transactional
    public void updateQnaComment(QnaCommentUpdate update) {
        QnaCommentEntity comment = selectQnaCommentById(update.getId());
        comment.update(update.getContent());
    }
}
