package com.corp.bookiki.chatbot.service;

import com.corp.bookiki.chatbot.dto.ChatbotRequest;
import com.corp.bookiki.chatbot.dto.ChatbotResponse;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.DialogflowException;
import com.corp.bookiki.user.dto.AuthUser;
import com.google.cloud.dialogflow.v2.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {
    private final SessionsClient sessionsClient;
    private final ContextsClient contextsClient;

    @Value("${dialogflow.project-id}")
    private String projectId;

    @Value("${dialogflow.language-code}")
    private String languageCode;

    public ChatbotResponse processMessage(ChatbotRequest request, AuthUser user) {
        try {
            SessionName session = SessionName.of(projectId, user.getId().toString());

            // 사용자 입력 처리
            TextInput.Builder textInput = TextInput.newBuilder()
                    .setText(request.getMessage())
                    .setLanguageCode(languageCode);

            QueryInput queryInput = QueryInput.newBuilder()
                    .setText(textInput)
                    .build();

            // 컨텍스트 및 파라미터 설정
            QueryParameters.Builder parameters = QueryParameters.newBuilder();

            // Dialogflow 요청 생성
            DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder()
                    .setSession(session.toString())
                    .setQueryInput(queryInput)
                    .setQueryParams(parameters)
                    .build();

            // Dialogflow 응답 처리
            DetectIntentResponse response = sessionsClient.detectIntent(detectIntentRequest);
            QueryResult queryResult = response.getQueryResult();

            // 엔티티 추출
            Map<String, String> entities = extractEntities(queryResult);

            // 빠른 응답 옵션 생성
            List<String> quickReplies = generateQuickReplies(queryResult);

            // 응답 생성
            return ChatbotResponse.builder()
                    .message(queryResult.getFulfillmentText())
                    .followUpMessage(getFollowUpMessage(queryResult))
                    .quickReplies(quickReplies)
                    .intent(queryResult.getIntent().getDisplayName())
                    .entities(entities)
                    .confidenceScore(queryResult.getIntentDetectionConfidence())
                    .requiresFollowUp(requiresFollowUp(queryResult))
                    .build();

        } catch (Exception e) {
            log.error("Dialogflow 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new DialogflowException(ErrorCode.DIALOGFLOW_PROCESSING_ERROR);
        }
    }

    private Map<String, String> extractEntities(QueryResult queryResult) {
        Map<String, String> entities = new HashMap<>();
        queryResult.getParameters().getFieldsMap().forEach((key, value) -> {
            if (!value.getStringValue().isEmpty()) {
                entities.put(key, value.getStringValue());
            }
        });
        return entities;
    }

    private List<String> generateQuickReplies(QueryResult queryResult) {
        List<String> quickReplies = new ArrayList<>();
        String intent = queryResult.getIntent().getDisplayName();

        // intent에 따른 빠른 응답 옵션 생성
        switch (intent) {
            case "qr_troubleshooting":
                quickReplies.addAll(Arrays.asList(
                        "QR 오류 신고하기",
                        "관리자 호출",
                        "다른 방법으로 대출하기"
                ));
                break;
            case "led_troubleshooting":
                quickReplies.addAll(Arrays.asList(
                        "LED 문제 신고",
                        "책 위치 다시 찾기",
                        "관리자 호출하기"
                ));
                break;
            case "book_category_guide":
                quickReplies.addAll(Arrays.asList(
                        "책 위치 찾기",
                        "대출 방법 안내",
                        "도서 추천 받기"
                ));
                break;
        }
        return quickReplies;
    }

    private String getFollowUpMessage(QueryResult queryResult) {
        // intent에 따른 후속 메시지 생성
        String intent = queryResult.getIntent().getDisplayName();
        switch (intent) {
            case "qr_troubleshooting":
                return "문제가 지속되면 'QR 오류 신고' 또는 '관리자 호출'을 선택해주세요.";
            case "led_troubleshooting":
                return "다른 도움이 필요하시다면 알려주세요.";
            default:
                return null;
        }
    }

    private boolean requiresFollowUp(QueryResult queryResult) {
        // 특정 intent에 대해 후속 조치가 필요한지 확인
        String intent = queryResult.getIntent().getDisplayName();
        return Arrays.asList(
                "qr_error_report",
                "led_troubleshooting",
                "book_location_guide"
        ).contains(intent);
    }
}