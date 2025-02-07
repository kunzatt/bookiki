package com.corp.bookiki.chatbot.entity;

import lombok.Getter;

@Getter
public enum FeedbackStatus {
    PENDING("처리 대기"),
    IN_PROGRESS("처리 중"),
    COMPLETED("처리 완료");

    private final String description;

    FeedbackStatus(String description) {
        this.description = description;
    }
}
