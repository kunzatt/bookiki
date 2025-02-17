package com.corp.bookiki.chatbot.service;

import com.corp.bookiki.chatbot.dto.ChatbotFeedbackRequest;
import com.corp.bookiki.chatbot.dto.ChatbotRequest;
import com.corp.bookiki.chatbot.dto.ChatbotResponse;
import com.corp.bookiki.chatbot.entity.ChatbotFeedbackEntity;
import com.corp.bookiki.chatbot.repository.ChatbotFeedbackRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.ChatbotException;
import com.corp.bookiki.global.error.exception.DialogflowException;
import com.corp.bookiki.user.dto.AuthUser;
import com.google.cloud.dialogflow.v2.*;
import com.google.protobuf.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {
    private final SessionsClient sessionsClient;
    private final ContextsClient contextsClient;
    private final ChatbotFeedbackRepository chatbotFeedbackRepository;

    // Value import문이 겹쳐서 따로 표기
    @org.springframework.beans.factory.annotation.Value("${dialogflow.project-id}")
    private String projectId;

    @org.springframework.beans.factory.annotation.Value("${chatbot.confidence.threshold}")
    private float confidenceThreshold;

    @org.springframework.beans.factory.annotation.Value("${dialogflow.language-code}")
    private String languageCode;

    public ChatbotResponse createMessage(ChatbotRequest request, Integer userId) {
        try {
            // 1. 세션 설정
            // 각 사용자별 대화 컨텍스트 관리
            SessionName session = SessionName.of(projectId, userId.toString());

            // 사용자 입력 설정
            // 사용자 입력을 Dialogflow 형식으로 변환
            TextInput.Builder textInput = TextInput.newBuilder()
                    .setText(request.getMessage())
                    .setLanguageCode(languageCode);

            // 3. 컨텍스트 및 파라미터 설정
            // 컨텍스트나 추가 설정을 위한 파라미터
            QueryParameters.Builder parameters = QueryParameters.newBuilder();

            // 이전 컨텍스트 가져오기
            ListContextsRequest contextsRequest = ListContextsRequest.newBuilder()
                    .setParent(session.toString())
                    .build();
            for (Context context : contextsClient.listContexts(contextsRequest).getPage().getValues()) {
                parameters.addContexts(context);
            }

            // 4. Dialogflow 요청 생성 및 전송
            DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder()
                    .setSession(session.toString())
                    .setQueryInput(QueryInput.newBuilder().setText(textInput).build())
                    .setQueryParams(parameters)
                    .build();

            // 5. 응답 처리
            // Dialogflow API 호출
            DetectIntentResponse response = sessionsClient.detectIntent(detectIntentRequest);
            QueryResult queryResult = response.getQueryResult();

            // 6. 신뢰도 검사
            if (queryResult.getIntentDetectionConfidence() < confidenceThreshold) {
                return handleLowConfidence(queryResult);
            }

            // 7. 엔티티 추출
            Map<String, Value> params = queryResult.getParameters().getFieldsMap();

            // 8. 응답 생성
            return createEnhancedResponse(queryResult, params);

        } catch (Exception e) {
            log.error("Dialogflow 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new DialogflowException(ErrorCode.DIALOGFLOW_PROCESSING_ERROR);
        }
    }

    private ChatbotResponse handleLowConfidence(QueryResult queryResult) {
        // 신뢰도가 낮은 경우의 처리
        return ChatbotResponse.builder()
                .message("죄송합니다. 질문을 더 자세히 말씀해 주시겠어요?")
                .build();
    }

    private ChatbotResponse createEnhancedResponse(QueryResult queryResult, Map<String, Value> params) {
        try {
            String intent = queryResult.getIntent().getDisplayName();

            // 1. Dialogflow의 기본 응답 처리
            StringBuilder messageBuilder = new StringBuilder(queryResult.getFulfillmentText());

            try {
                // 2. 대화 흐름 확인
                if (!queryResult.getAllRequiredParamsPresent()) {
                    messageBuilder.append("\n").append(queryResult.getDiagnosticInfo());
                }
            } catch (Exception e) {
                log.error("대화 흐름 확인 중 오류 발생: {}", e.getMessage());
                throw new ChatbotException(ErrorCode.CONTEXT_MANAGEMENT_ERROR);
            }

            return ChatbotResponse.builder()
                    .message(messageBuilder.toString())
                    .showAdminInquiryButton(shouldShowAdminInquiryButton(intent))
                    .build();

        } catch (ChatbotException e) {
            throw e;
        } catch (Exception e) {
            log.error("응답 생성 중 오류 발생: {}", e.getMessage());
            throw new ChatbotException(ErrorCode.DIALOGFLOW_PROCESSING_ERROR);
        }
    }

    private String buildFullMessage(QueryResult queryResult) {
        try {
            StringBuilder messageBuilder = new StringBuilder(queryResult.getFulfillmentText());

            String followUpMsg = createFollowUpMessage(queryResult.getIntent().getDisplayName());
            if (followUpMsg != null && !followUpMsg.isEmpty()) {
                messageBuilder.append("\n\n").append(followUpMsg);
            }

            return messageBuilder.toString();
        } catch (Exception e) {
            log.error("메시지 생성 중 오류 발생: {}", e.getMessage());
            throw new ChatbotException(ErrorCode.CONTEXT_MANAGEMENT_ERROR);
        }
    }

    private String createFollowUpMessage(String intent) {
        try {
            // 특정 intent에 대해 추가 안내 메시지 제공
            switch (intent) {
                case "qr_troubleshooting", "led_troubleshooting":
                    return "문제가 지속되면 '오류 신고 및 피드백'을 선택해주세요.";
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("후속 메시지 생성 중 오류 발생: {}", e.getMessage());
            throw new ChatbotException(ErrorCode.CONTEXT_MANAGEMENT_ERROR);
        }
    }

    private boolean shouldShowAdminInquiryButton(String intent) {
        try {
            // 관리자 문의가 필요할 수 있는 상황들
            return Arrays.asList(
                    "qr_troubleshooting",
                    "led_troubleshooting",
                    "wrong_shelf_report",
                    "system_status_check",
                    "qr_error_report",
                    "led_brightness_issue",
                    "emergency_support",
                    "camera_status",
                    "book_wrong_place",
                    "book_damage_report",
                    "book_condition_report",
                    "auto_return_guide"
            ).contains(intent);
        } catch (Exception e) {
            log.error("관리자 문의 버튼 표시 여부 확인 중 오류 발생: {}", e.getMessage());
            throw new ChatbotException(ErrorCode.INTENT_PROCESSING_ERROR);
        }
    }

    // 관리자에게 추가 문의 처리 로직
    public void createFeedback(ChatbotFeedbackRequest request, Integer userId) {
        try {
            log.info("사용자 피드백 접수 - 사용자: {}, Intent: {}, 카테고리: {}",
                     userId, request.getOriginalIntent(), request.getCategory());
            ChatbotFeedbackEntity feedback = ChatbotFeedbackEntity.builder()
                    .userId(userId)
                    .originalIntent(request.getOriginalIntent())
                    .feedbackMessage(request.getFeedbackMessage())
                    .category(request.getCategory())
                    .build();

            chatbotFeedbackRepository.save(feedback);
        } catch (Exception e) {
            log.error("피드백 생성 중 오류 발생: {}", e.getMessage());
            throw new ChatbotException(ErrorCode.CONTEXT_MANAGEMENT_ERROR);
        }
    }
}