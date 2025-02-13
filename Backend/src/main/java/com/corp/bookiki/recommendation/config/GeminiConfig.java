package com.corp.bookiki.recommendation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GeminiConfig {

    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;

    @Bean
    public RestTemplate geminiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // RestTemplate 설정 (예: timeout, error handler 등)
        return restTemplate;
    }

    @Bean
    public String geminiApiKey() {
        return apiKey;
    }
}