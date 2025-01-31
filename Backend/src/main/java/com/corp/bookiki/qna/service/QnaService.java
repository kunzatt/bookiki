package com.corp.bookiki.qna.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaException;
import com.corp.bookiki.qna.QnaTestConstants;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaDetailResponse;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.dto.QnaUpdate;
import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import com.corp.bookiki.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QnaService {

    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;

    // 문의사항 등록
    @Transactional
    public int createQna(QnaRequest request, int authorId) {
        // user 관련 서비스 완성 후 수정
        QnaEntity qna = request.toEntity(authorId);
        qnaRepository.save(qna);
        return qna.getId();
    }

    // 문의사항 조회
    @Transactional(readOnly = true)
    public List<QnaEntity> selectQnas(String keyword, String qnaType, Boolean answered) {
        // 1. 기본 목록 가져오기
        List<QnaEntity> filteredQnas = qnaRepository.findByDeletedFalseOrderByCreatedAtDesc();

        // 2. 답변 유무로 필터링
        if (answered != null) {
            List<QnaEntity> answerFilteredQnas = new ArrayList<>();
            for (QnaEntity qnaEntity : filteredQnas) {
                if (answered == checkHasComment(qnaEntity)) {
                    answerFilteredQnas.add(qnaEntity);
                }
            }
            filteredQnas = answerFilteredQnas;
        }

        // 3. 문의사항 유형으로 필터링
        if (qnaType != null && !qnaType.isBlank()) {
            List<QnaEntity> typeFilteredQnas = new ArrayList<>();
            for (QnaEntity qnaEntity : filteredQnas) {
                if (qnaEntity.getQnaType().equals(qnaType)) {
                    typeFilteredQnas.add(qnaEntity);
                }
            }
            filteredQnas = typeFilteredQnas;
        }

        // 4. 키워드로 검색
        if (keyword != null && !keyword.isBlank()) {
            List<QnaEntity> keywordFilteredQnas = new ArrayList<>();
            for (QnaEntity qnaEntity : filteredQnas) {
                if (qnaEntity.getTitle().contains(keyword) || qnaEntity.getContent().contains(keyword)) {
                    keywordFilteredQnas.add(qnaEntity);
                }
            }
            filteredQnas = keywordFilteredQnas;
        }

        return filteredQnas;
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
        List<QnaCommentEntity> comments = qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(id);
        List<QnaCommentResponse> responses = new ArrayList<>();
        for (QnaCommentEntity comment : comments) {
            responses.add(new QnaCommentResponse(comment));
        }
        return new QnaDetailResponse(qna, QnaTestConstants.TEST_USER_NAME, responses);
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
        qna.update(update.getTitle(), update.getQnaType(), update.getContent());
    }

    // 문의사항 답변 여부 판별
    public boolean checkHasComment(QnaEntity qna) {
        List<QnaCommentEntity> comments = qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(qna.getId());
        return !comments.isEmpty();
    }
}
