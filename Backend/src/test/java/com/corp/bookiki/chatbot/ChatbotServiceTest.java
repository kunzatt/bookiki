package com.corp.bookiki.chatbot;

import com.corp.bookiki.chatbot.dto.ChatbotRequest;
import com.corp.bookiki.chatbot.dto.ChatbotResponse;
import com.corp.bookiki.chatbot.service.ChatbotService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.DialogflowException;
import com.corp.bookiki.user.dto.AuthUser;
import com.google.cloud.dialogflow.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ChatbotServiceTest {
    @Mock
    private SessionsClient sessionsClient;

    @Mock
    private ContextsClient contextsClient;

    @InjectMocks
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatbotService, "projectId", "test-project");
        ReflectionTestUtils.setField(chatbotService, "languageCode", "ko-KR");
    }

    @Test
    @DisplayName("메시지 처리 성공 테스트")
    void processMessageSuccess() {
        // given
        ChatbotRequest request = ChatbotRequest.builder()
                .message("도서관 이용시간 알려줘")
                .build();

        AuthUser user = AuthUser.builder()
                .id(1)
                .build();

        DetectIntentResponse mockResponse = mock(DetectIntentResponse.class);
        QueryResult mockQueryResult = mock(QueryResult.class);
        Intent mockIntent = mock(Intent.class);

        when(mockResponse.getQueryResult()).thenReturn(mockQueryResult);
        when(mockQueryResult.getFulfillmentText()).thenReturn("도서관은 9시부터 6시까지 이용 가능합니다.");
        when(mockQueryResult.getIntent()).thenReturn(mockIntent);
        when(mockIntent.getDisplayName()).thenReturn("library_hours");
        when(mockQueryResult.getIntentDetectionConfidence()).thenReturn(0.8f);
        when(sessionsClient.detectIntent(any())).thenReturn(mockResponse);

        // when
        ChatbotResponse response = chatbotService.processMessage(request, user);

        // then
        assertThat(response.getMessage()).isEqualTo("도서관은 9시부터 6시까지 이용 가능합니다.");
        assertThat(response.getIntent()).isEqualTo("library_hours");
        assertThat(response.getConfidenceScore()).isEqualTo(0.8f);
    }

    @Test
    @DisplayName("Dialogflow 처리 실패 테스트")
    void processMessageFailure() {
        // given
        ChatbotRequest request = ChatbotRequest.builder()
                .message("테스트 메시지")
                .build();

        AuthUser user = AuthUser.builder()
                .id(1)
                .build();

        when(sessionsClient.detectIntent(any())).thenThrow(new RuntimeException("API 오류"));

        // when & then
        assertThatThrownBy(() -> chatbotService.processMessage(request, user))
                .isInstanceOf(DialogflowException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIALOGFLOW_PROCESSING_ERROR);
    }
}
