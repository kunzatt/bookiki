package com.corp.bookiki.chatbot.service;

import com.corp.bookiki.chatbot.dto.ChatbotFeedbackResponse;
import com.corp.bookiki.chatbot.entity.ChatbotFeedbackEntity;
import com.corp.bookiki.chatbot.entity.FeedbackStatus;
import com.corp.bookiki.chatbot.repository.ChatbotFeedbackRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.ChatbotException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    public Page<ChatbotFeedbackResponse> selectAllFeedbacks(
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

                    predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

                    if (status != null) {
                        predicates.add(criteriaBuilder.equal(root.get("status"), status));
                    }

                    if (category != null && !category.isEmpty()) {
                        predicates.add(criteriaBuilder.equal(root.get("category"), category));
                    }

                    if (startDate != null && endDate != null) {
                        predicates.add(criteriaBuilder.between(root.get("createdAt"), startDate, endDate));
                    }

                    Predicate[] predicateArray = new Predicate[predicates.size()];
                    predicates.toArray(predicateArray);
                    return criteriaBuilder.and(predicateArray);
                }
            };

            Page<ChatbotFeedbackEntity> entityPage = chatbotFeedbackRepository.findAll(spec, pageable);
            return new PageImpl<>(
                    convertToResponseList(entityPage.getContent()),
                    pageable,
                    entityPage.getTotalElements()
            );

        } catch (Exception e) {
            log.error("피드백 목록 조회 중 오류 발생: {}", e.getMessage());
            throw new ChatbotException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 특정 피드백 조회
    public ChatbotFeedbackResponse selectFeedbackById(Integer id) {
        try {
            ChatbotFeedbackEntity entity = selectFeedbackEntity(id);
            return ChatbotFeedbackResponse.from(entity);
        } catch (ChatbotException e) {
            throw e;
        } catch (Exception e) {
            log.error("피드백 조회 중 오류 발생 - ID: {}, 에러: {}", id, e.getMessage());
            throw new ChatbotException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 피드백 상태 업데이트
    @Transactional
    public ChatbotFeedbackResponse updateFeedbackStatus(Integer id, FeedbackStatus newStatus) {
        try {
            ChatbotFeedbackEntity entity = selectFeedbackEntity(id);
            entity.updateStatus(newStatus);
            ChatbotFeedbackEntity savedEntity = chatbotFeedbackRepository.save(entity);
            return ChatbotFeedbackResponse.from(savedEntity);
        } catch (ChatbotException e) {
            throw e;
        } catch (Exception e) {
            log.error("피드백 상태 업데이트 중 오류 발생 - ID: {}, 상태: {}, 에러: {}",
                    id, newStatus, e.getMessage());
            throw new ChatbotException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 특정 사용자의 피드백 내역 조회
    public List<ChatbotFeedbackResponse> selectFeedbackByUserId(int userId) {
        try {
            List<ChatbotFeedbackEntity> entityList = chatbotFeedbackRepository
                    .findByUserIdAndDeletedFalseOrderByIdDesc(userId);
            return convertToResponseList(entityList);
        } catch (Exception e) {
            log.error("사용자 피드백 조회 중 오류 발생 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
            throw new ChatbotException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 내부용 메서드 - 엔티티 조회
    private ChatbotFeedbackEntity selectFeedbackEntity(Integer id) {
        try {
            ChatbotFeedbackEntity entity = chatbotFeedbackRepository.getReferenceById(id);
            if (entity.isDeleted()) {
                throw new ChatbotException(ErrorCode.CONTEXT_NOT_FOUND);
            }
            return entity;
        } catch (EntityNotFoundException e) {
            throw new ChatbotException(ErrorCode.CONTEXT_NOT_FOUND);
        }
    }

    // Entity 리스트를 Response 리스트로 변환
    private List<ChatbotFeedbackResponse> convertToResponseList(List<ChatbotFeedbackEntity> entityList) {
        List<ChatbotFeedbackResponse> responseList = new ArrayList<>();
        for (ChatbotFeedbackEntity entity : entityList) {
            responseList.add(ChatbotFeedbackResponse.from(entity));
        }
        return responseList;
    }
}