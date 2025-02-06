package com.corp.bookiki.chatbot.service;

import com.corp.bookiki.chatbot.entity.ChatbotFeedbackEntity;
import com.corp.bookiki.chatbot.entity.FeedbackStatus;
import com.corp.bookiki.chatbot.repository.ChatbotFeedbackRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.ChatbotException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotFeedbackService {
    private final ChatbotFeedbackRepository chatbotFeedbackRepository;

    // 모든 피드백 조회 (페이지네이션, 필터링 적용)
    public Page<ChatbotFeedbackEntity> selectAllFeedbacks(
            FeedbackStatus status,
            String category,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        try {
            Specification<ChatbotFeedbackEntity> spec = new Specification<ChatbotFeedbackEntity>() {
                @Override
                public Predicate toPredicate(Root<ChatbotFeedbackEntity> root,
                                             CriteriaQuery<?> query,
                                             CriteriaBuilder criteriaBuilder) {

                    List<Predicate> predicates = new ArrayList<>();

                    // 상태 필터
                    if (status != null) {
                        predicates.add(criteriaBuilder.equal(root.get("status"), status));
                    }

                    // 카테고리 필터
                    if (category != null && !category.isEmpty()) {
                        predicates.add(criteriaBuilder.equal(root.get("category"), category));
                    }

                    // 날짜 범위 필터
                    if (startDate != null && endDate != null) {
                        predicates.add(criteriaBuilder.between(root.get("createdAt"), startDate, endDate));
                    }

                    Predicate[] predicateArray = new Predicate[predicates.size()];
                    predicates.toArray(predicateArray);
                    return criteriaBuilder.and(predicateArray);
                }
            };

            return chatbotFeedbackRepository.findAll(spec, pageable);
        } catch (Exception e) {
            log.error("피드백 목록 조회 중 오류 발생: {}", e.getMessage());
            throw new ChatbotException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 특정 피드백 조회
    public ChatbotFeedbackEntity selectFeedbackById(Integer id) {
        try {
            ChatbotFeedbackEntity feedback = chatbotFeedbackRepository.getReferenceById(id);
            if (feedback == null) {
                throw new ChatbotException(ErrorCode.CONTEXT_NOT_FOUND);
            }
            return feedback;
        } catch (ChatbotException e) {
            throw e;
        } catch (Exception e) {
            log.error("피드백 조회 중 오류 발생 - ID: {}, 에러: {}", id, e.getMessage());
            throw new ChatbotException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 피드백 상태 업데이트
    @Transactional
    public ChatbotFeedbackEntity updateFeedbackStatus(Integer id, FeedbackStatus newStatus) {
        try {
            ChatbotFeedbackEntity feedback = selectFeedbackById(id);
            feedback.updateStatus(newStatus);
            return chatbotFeedbackRepository.save(feedback);
        } catch (ChatbotException e) {
            throw e;
        } catch (Exception e) {
            log.error("피드백 상태 업데이트 중 오류 발생 - ID: {}, 상태: {}, 에러: {}",
                    id, newStatus, e.getMessage());
            throw new ChatbotException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 특정 사용자의 피드백 내역 조회
    public List<ChatbotFeedbackEntity> selectFeedbackByUserId(int userId) {
        try {
            return chatbotFeedbackRepository.findByUserIdOrderByIdDesc(userId);
        } catch (Exception e) {
            log.error("사용자 피드백 조회 중 오류 발생 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
            throw new ChatbotException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
