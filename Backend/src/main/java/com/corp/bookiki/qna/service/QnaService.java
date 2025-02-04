package com.corp.bookiki.qna.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaDetailResponse;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.dto.QnaUpdate;
import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import com.corp.bookiki.qna.repository.QnaRepository;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
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
public class QnaService {

    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;
    private final UserRepository userRepository;

    // 문의사항 등록
    @Transactional
    public int createQna(QnaRequest request, int authorId) {
        try {
            QnaEntity qna = request.toEntity(authorId);
            qnaRepository.save(qna);
            return qna.getId();
        } catch (Exception ex) {
            log.error("문의사항 등록 실패: {}", ex.getMessage());
            throw new QnaException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 조회
    @Transactional(readOnly = true)
    public List<QnaEntity> selectQnas(String keyword, String qnaType, Boolean answered, AuthUser authUser) {
        try {
            // 1. 기본 목록 가져오기
            List<QnaEntity> filteredQnas;

            // 관리자는 모든 문의사항을 볼 수 있고, 사용자는 자신의 문의사항만 볼 수 있음
            if (authUser.getRole() == Role.ADMIN) {
                filteredQnas = qnaRepository.findByDeletedFalseOrderByCreatedAtDesc();
            } else {
                filteredQnas = qnaRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(authUser.getId());
            }

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
        } catch (Exception ex) {
            log.error("문의사항 목록 조회 실패: {}", ex.getMessage());
            throw new QnaException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 1개 조회
    @Transactional(readOnly = true)
    public QnaEntity selectQnaById(int id) {
        try {
            QnaEntity qna = qnaRepository.getReferenceById(id);
            if (qna == null || qna.isDeleted()) {
                throw new QnaException(ErrorCode.QNA_NOT_FOUND);
            }
            return qna;
        } catch (EntityNotFoundException e) {
            log.error("문의사항 조회 실패: 존재하지 않는 ID - {}", id);
            throw new QnaException(ErrorCode.QNA_NOT_FOUND);
        } catch (Exception e) {
            log.error("문의사항 조회 중 오류 발생: {}", e.getMessage());
            throw new QnaException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 댓글 목록까지 포함한 상세 조회 메서드
    @Transactional(readOnly = true)
    public QnaDetailResponse selectQnaByIdWithComment(int id, AuthUser authUser) {
        try {
            QnaEntity qna = selectQnaById(id);

            if (authUser.getRole() != Role.ADMIN && qna.getAuthorId() != authUser.getId()) {
                throw new QnaException(ErrorCode.UNAUTHORIZED);
            }

            UserEntity author;
            try {
                author = userRepository.getReferenceById(qna.getAuthorId());
            } catch (EntityNotFoundException e) {
                throw new UserException(ErrorCode.USER_NOT_FOUND);
            }

            List<QnaCommentEntity> comments =
                    qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(id);
            List<QnaCommentResponse> commentResponses = new ArrayList<>();

            for (QnaCommentEntity comment : comments) {
                commentResponses.add(new QnaCommentResponse(comment));
            }

            return new QnaDetailResponse(qna, author.getUserName(), commentResponses);
        } catch (QnaException | UserException e) {
            log.error("문의사항 상세 조회 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("문의사항 상세 조회 중 예상치 못한 오류 발생: {}", e.getMessage());
            throw new QnaException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 삭제
    @Transactional
    public void deleteQna(int id, int currentUserId) {
        try {
            QnaEntity qna = selectQnaById(id);
            if (qna.getAuthorId() != currentUserId) {
                throw new QnaException(ErrorCode.UNAUTHORIZED);
            }
            qna.delete();
            qnaRepository.save(qna);
        } catch (QnaException e) {
            log.error("문의사항 삭제 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("문의사항 삭제 중 오류 발생: {}", e.getMessage());
            throw new QnaException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 수정
    @Transactional
    public void updateQna(QnaUpdate update, int currentUserId) {
        try {
            QnaEntity qna = selectQnaById(update.getId());
            if (qna.getAuthorId() != currentUserId) {
                throw new QnaException(ErrorCode.UNAUTHORIZED);
            }
            qna.update(update.getTitle(), update.getQnaType(), update.getContent());
            qnaRepository.save(qna);
        } catch (QnaException e) {
            log.error("문의사항 수정 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("문의사항 수정 중 오류 발생: {}", e.getMessage());
            throw new QnaException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 답변 여부 판별
    private boolean checkHasComment(QnaEntity qna) {
        try {
            List<QnaCommentEntity> comments =
                    qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(qna.getId());
            return !comments.isEmpty();
        } catch (Exception e) {
            log.error("문의사항 답변 확인 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
}
