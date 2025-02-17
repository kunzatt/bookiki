package com.corp.bookiki.chatbot.repository;

import com.corp.bookiki.chatbot.entity.ChatbotFeedbackEntity;
import com.corp.bookiki.chatbot.entity.FeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatbotFeedbackRepository extends JpaRepository<ChatbotFeedbackEntity, Integer>, JpaSpecificationExecutor<ChatbotFeedbackEntity> {

    // 상태로 검색 (삭제되지 않은 것만)
    List<ChatbotFeedbackEntity> findByStatusAndDeletedFalseOrderByIdDesc(FeedbackStatus status);

    // 카테고리로 검색 (삭제되지 않은 것만)
    List<ChatbotFeedbackEntity> findByCategoryAndDeletedFalseOrderByIdDesc(String category);

    // 날짜 범위로 검색 (삭제되지 않은 것만)
    List<ChatbotFeedbackEntity> findByCreatedAtBetweenAndDeletedFalseOrderByIdDesc(LocalDateTime startDate, LocalDateTime endDate);

    // 사용자 ID로 검색 (삭제되지 않은 것만)
    List<ChatbotFeedbackEntity> findByUserIdAndDeletedFalseOrderByIdDesc(int userId);
}
