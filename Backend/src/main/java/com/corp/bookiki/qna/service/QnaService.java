package com.corp.bookiki.qna.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.notification.entity.NotificationInformation;
import com.corp.bookiki.notification.service.NotificationService;
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
import com.corp.bookiki.user.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private final UserService userService;
    private final NotificationService notificationService;

    // 문의사항 등록
    @Transactional
    public int createQna(QnaRequest request, Integer userId) {
        try {
            UserEntity user = userService.getUserById(userId);
            QnaEntity qna = request.toEntity(user);
            qnaRepository.save(qna);
            notificationService.addQnaCreatedNotification(NotificationInformation.QNA_CREATED, qna.getTitle(), qna.getId());
            return qna.getId();
        } catch (Exception ex) {
            log.error("문의사항 등록 실패: {}", ex.getMessage());
            throw new QnaException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 조회
    @Transactional(readOnly = true)
    public Page<QnaEntity> selectQnas(String keyword, String qnaType, Boolean answered, AuthUser authUser, Pageable pageable) {
        try {
            // 관리자가 아닌 경우에만 authorId 설정
            Integer authorId = null;
            if (authUser.getRole() != Role.ADMIN) {
                authorId = authUser.getId();
            }

            // 동적 쿼리로 기본 검색 수행
            Page<QnaEntity> qnaPage = qnaRepository.findBySearchCriteria(
                    authorId,
                    qnaType,
                    keyword,
                    pageable
            );

            // answered 필터링이 필요한 경우
            if (answered != null) {
                List<QnaEntity> pageContent = qnaPage.getContent();
                List<QnaEntity> filteredContent = new ArrayList<>();

                for (QnaEntity qna : pageContent) {
                    if (answered == checkHasComment(qna)) {
                        filteredContent.add(qna);
                    }
                }

                // 필터링된 결과로 새로운 Page 객체 생성
                return new PageImpl<>(
                        filteredContent,
                        pageable,
                        filteredContent.size()
                );
            }

            return qnaPage;
        } catch (Exception ex) {
            log.error("문의사항 목록 조회 실패: {}", ex.getMessage());
            throw new QnaException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의사항 1개 조회
    @Transactional(readOnly = true)
    public QnaEntity selectQnaById(Integer id) {
        try {
            QnaEntity qna = qnaRepository.getReferenceById(id);
            if (qna.isDeleted()) {
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
    public QnaDetailResponse selectQnaByIdWithComment(Integer id, AuthUser authUser) {
        try {
            QnaEntity qna = selectQnaById(id);

            if (authUser.getRole() != Role.ADMIN && (qna.getUser().getId() != authUser.getId())) {
                throw new QnaException(ErrorCode.UNAUTHORIZED);
            }

            List<QnaCommentEntity> comments =
                    qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(id);
            List<QnaCommentResponse> commentResponses = new ArrayList<>();

            for (QnaCommentEntity comment : comments) {
                commentResponses.add(new QnaCommentResponse(comment));
            }

            return new QnaDetailResponse(qna,commentResponses);
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
    public void deleteQna(Integer id, Integer currentUserId) {
        try {
            QnaEntity qna = selectQnaById(id);
            if (qna.getUser().getId() != currentUserId) {
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
    public void updateQna(QnaUpdate update, Integer currentUserId) {
        try {
            QnaEntity qna = selectQnaById(update.getId());
            if (currentUserId == null || qna.getUser().getId() != currentUserId) {
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
