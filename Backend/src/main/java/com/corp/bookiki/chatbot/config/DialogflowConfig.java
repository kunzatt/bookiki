package com.corp.bookiki.chatbot.config;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.DialogflowException;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.ContextsClient;
import com.google.cloud.dialogflow.v2.ContextsSettings;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DialogflowConfig {

    @Value("${dialogflow.project-id}")
    private String projectId;

    @Value("${dialogflow.credentials-path}")
    private String credentialsPath;

    @Value("${dialogflow.language-code}")
    private String languageCode;

    @Bean
    public SessionsClient dialogflowSessionsClient() throws IOException {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsPath)
            );

            SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            return SessionsClient.create(sessionsSettings);
        } catch (IOException e) {
            log.error("Dialogflow 클라이언트 생성 실패: {}", e.getMessage());
            throw new DialogflowException(ErrorCode.DIALOGFLOW_CLIENT_ERROR);
        }
    }

    @Bean
    public ContextsClient dialogflowContextsClient() throws IOException {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsPath)
            );

            ContextsSettings contextsSettings = ContextsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            return ContextsClient.create(contextsSettings);
        } catch (IOException e) {
            log.error("Dialogflow 컨텍스트 클라이언트 생성 실패: {}", e.getMessage());
            throw new DialogflowException(ErrorCode.DIALOGFLOW_CLIENT_ERROR);
        }
    }

    @Bean
    @Qualifier("dialogflowProjectId")
    public String dialogflowProjectId() {
        return projectId;
    }

    @Bean
    @Qualifier("dialogflowLanguageCode")
    public String dialogflowLanguageCode() {
        return languageCode;
    }
}
