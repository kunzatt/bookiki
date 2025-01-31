package com.corp.bookiki.qna.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaCommentException;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
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

    @Transactional
    public List<QnaCommentResponse> selectCommentsByQnaId(int qnaId) {
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
}
