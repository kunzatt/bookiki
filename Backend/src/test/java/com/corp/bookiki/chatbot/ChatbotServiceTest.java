package com.corp.bookiki.chatbot;

import com.corp.bookiki.chatbot.service.ChatbotService;
import com.google.cloud.dialogflow.v2.ContextsClient;
import com.google.cloud.dialogflow.v2.SessionsClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    
}
