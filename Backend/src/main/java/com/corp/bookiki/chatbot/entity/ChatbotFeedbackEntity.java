package com.corp.bookiki.chatbot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatbot_feedback")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotFeedbackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "original_intent")
    private String originalIntent;

    @Column(name = "feedback_message", nullable = false)
    private String feedbackMessage;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeedbackStatus status;

    @Builder
    public ChatbotFeedbackEntity(int userId, String originalIntent,
                                 String feedbackMessage, String category) {
        this.userId = userId;
        this.originalIntent = originalIntent;
        this.feedbackMessage = feedbackMessage;
        this.category = category;
        this.status = FeedbackStatus.PENDING;
    }

    public void updateStatus(FeedbackStatus status) {
        this.status = status;
    }
}
