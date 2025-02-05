package com.corp.bookiki.qna.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaCommentException;
import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import jakarta.persistence.EntityNotFoundException;
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
            log.error("QNA 댓글 목록 조회 실패: qnaId={}, error={}", qnaId, ex.getMessage());
            throw new QnaCommentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 1개 조회
    @Transactional(readOnly = true)
    public QnaCommentEntity selectQnaCommentById(int commentId) {
        try {
            QnaCommentEntity comment = qnaCommentRepository.getReferenceById(commentId);

            if (comment.isDeleted()) {
                throw new QnaCommentException(ErrorCode.COMMENT_NOT_FOUND);
            }

            return comment;
        } catch (EntityNotFoundException ex) {
            log.error("QNA 댓글을 찾을 수 없음: commentId={}", commentId);
            throw new QnaCommentException(ErrorCode.COMMENT_NOT_FOUND);
        } catch (QnaCommentException e) {
            throw e;
        } catch (Exception ex) {
            log.error("QNA 댓글 조회 실패: commentId={}, error={}", commentId, ex.getMessage());
            throw new QnaCommentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 답변 등록
    @Transactional
    public int createQnaComment(QnaCommentRequest request, int authorId) {
        try {
            // 해당 문의사항 존재 확인
            qnaService.selectQnaById(request.getQnaId());

            if (request.getContent() == null || request.getContent().isBlank()) {
                throw new QnaCommentException(ErrorCode.INVALID_INPUT_VALUE);
            }

            QnaCommentEntity comment = request.toEntity(authorId);
            QnaCommentEntity savedComment = qnaCommentRepository.save(comment);

            return savedComment.getId();
        } catch (QnaCommentException e) {
            throw e;
        } catch (Exception ex) {
            log.error("QNA 댓글 등록 실패: qnaId={}, authorId={}, error={}",
                    request.getQnaId(), authorId, ex.getMessage());
            throw new QnaCommentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 답변 삭제
    @Transactional
    public void deleteQnaComment(int commentId) {
        try {
            QnaCommentEntity comment = selectQnaCommentById(commentId);

            if (comment.isDeleted()) {
                throw new QnaCommentException(ErrorCode.COMMENT_NOT_FOUND);
            }

            comment.delete();
        } catch (QnaCommentException e) {
            throw e;
        } catch (Exception ex) {
            log.error("QNA 댓글 삭제 실패: commentId={}, error={}", commentId, ex.getMessage());
            throw new QnaCommentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 답변 수정
    @Transactional
    public void updateQnaComment(QnaCommentUpdate update) {
        try {
            QnaCommentEntity comment = selectQnaCommentById(update.getId());

            if (update.getContent() == null || update.getContent().isBlank()) {
                throw new QnaCommentException(ErrorCode.INVALID_INPUT_VALUE);
            }

            comment.update(update.getContent());
        } catch (QnaCommentException e) {
            throw e;
        } catch (Exception ex) {
            log.error("QNA 댓글 수정 실패: commentId={}, error={}",
                    update.getId(), ex.getMessage());
            throw new QnaCommentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}