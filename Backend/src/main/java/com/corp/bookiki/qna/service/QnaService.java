package com.corp.bookiki.qna.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaException;
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
    private final QnaRepository qnaRepository;

    // 문의사항 등록
    @Transactional
    public int creatQna(QnaRequest request, int authorId) {
        // user 관련 서비스 완성 후 수정
        QnaEntity qna = request.toEntity(authorId);
        qnaRepository.save(qna);
        return qna.getId();
    }

    // 문의사항 전체 조회 (정렬, 검색 포함)
    @Transactional(readOnly = true)
    public List<QnaEntity> selectAllQnas(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return qnaRepository.findByDeletedFalseOrderByCreatedAtDesc()
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
