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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DialogflowConfig {

    @Value("${dialogflow.project-id}")
    private String projectId;

    @Value("${dialogflow.client-email}")
    private String clientEmail;

    @Value("${dialogflow.private-key}")
    private String privateKey;

    @Value("${dialogflow.language-code}")
    private String languageCode;

    private GoogleCredentials createCredentials() throws IOException {
        String credentialJson = String.format("""
                        {
                          "type": "service_account",
                          "project_id": "%s",
                          "private_key_id": "%s",
                          "private_key": "%s",
                          "client_email": "%s",
                          "client_id": "%s",
                          "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                          "token_uri": "https://oauth2.googleapis.com/token",
                          "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                          "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/%s",
                          "universe_domain": "googleapis.com"
                        }""",
                projectId,
                "private-key-id-" + UUID.randomUUID().toString(),
                privateKey.replace("\\n", "\n"),
                clientEmail,
                UUID.randomUUID().toString(),
                clientEmail
        );

        return GoogleCredentials.fromStream(
                new ByteArrayInputStream(credentialJson.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Bean
    public SessionsClient dialogflowSessionsClient() {
        try {
            log.debug("Creating Dialogflow credentials with:");
            log.debug("Project ID: {}", projectId);
            log.debug("Client Email: {}", clientEmail);

            GoogleCredentials credentials = createCredentials();
            SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            return SessionsClient.create(sessionsSettings);

        } catch (IOException e) {
            log.error("Dialogflow 클라이언트 생성 실패: {}", e.getMessage(), e);
            throw new DialogflowException(ErrorCode.DIALOGFLOW_CLIENT_ERROR);
        }
    }

    @Bean
    public ContextsClient dialogflowContextsClient() {
        try {
            GoogleCredentials credentials = createCredentials();
            ContextsSettings contextsSettings = ContextsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            return ContextsClient.create(contextsSettings);

        } catch (IOException e) {
            log.error("Dialogflow Contexts 클라이언트 생성 실패: {}", e.getMessage(), e);
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