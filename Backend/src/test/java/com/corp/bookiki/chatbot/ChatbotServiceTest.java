package com.corp.bookiki.chatbot;

import com.corp.bookiki.chatbot.dto.ChatbotRequest;
import com.corp.bookiki.chatbot.service.ChatbotService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.DialogflowException;
import com.corp.bookiki.user.dto.AuthUser;
import com.google.cloud.dialogflow.v2.ContextsClient;
import com.google.cloud.dialogflow.v2.SessionsClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
