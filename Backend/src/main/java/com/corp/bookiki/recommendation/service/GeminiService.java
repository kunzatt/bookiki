package com.corp.bookiki.recommendation.service;

import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookinformation.dto.BookInformationResponse;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.GeminiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class GeminiService {
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;

    public GeminiService(
            RestTemplate restTemplate,
            @Value("${spring.ai.gemini.api-key}") String apiKey,
            @Value("${spring.ai.gemini.url}") String apiUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String getRecommendationKeywords(List<BookHistoryResponse> borrowHistory) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Based on these books, suggest 5 search keywords for finding similar books: \n\n");

            for (BookHistoryResponse history : borrowHistory) {
                prompt.append("- ").append(history.getBookTitle())
                        .append(" by ").append(history.getBookAuthor())
                        .append("\n");
            }

            prompt.append("\nProvide only keywords separated by commas, no explanations. Provide it in Korean, " +
                    "and ensure each keyword is a single searchable word without spaces, " +
                    "suitable for use in an SQL LIKE statement.");

            String response = callGeminiAPI(prompt.toString());
            log.debug("Gemini 실제 응답: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생: ", e);
            // 기본 키워드로 폴백
            return "general, popular, recommended, new, trending";
        }
    }

    public BookInformationEntity getCategoryForBook(BookInformationEntity bookInfo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("As a librarian, classify this book according to the Dewey Decimal Classification system. ")
                .append("Based on the main subject matter, which top-level DDC category (000-900) best fits this book? ")
                .append("Respond with ONLY a single number (0-9) corresponding to the first digit of the DDC classification.\n\n")
                .append("Book Information:\n")
                .append("Title: ").append(bookInfo.getTitle()).append("\n")
                .append("Author: ").append(bookInfo.getAuthor()).append("\n")
                .append("Description: ").append(bookInfo.getDescription()).append("\n");

        // Gemini API 호출
        String response = callGeminiAPI(prompt.toString());

        try {
            // 응답에서 숫자만 추출
            String numberOnly = response.replaceAll("[^0-9]", "").trim();
            bookInfo.updateCategory(Integer.parseInt(numberOnly));
            return bookInfo;
        } catch (Exception e) {
            log.error("Failed to parse category from Gemini response: {}", response, e);
            // 기본값으로 문학(8) 반환
            throw new GeminiException(ErrorCode.GEMINI_RESPONSE_ERROR);
        }
    }

    private String callGeminiAPI(String prompt) {
        String fullUrl = apiUrl + ":generateContent?key=" + apiKey;

        // Request 구성
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contentData = new HashMap<>();
        contentData.put("text", prompt);
        List<Map<String, Object>> requestParts = new ArrayList<>();
        requestParts.add(contentData);
        Map<String, Object> contentWrapper = new HashMap<>();
        contentWrapper.put("parts", requestParts);
        requestBody.put("contents", Collections.singletonList(contentWrapper));

        try {
            log.debug("Gemini API Request: {}", requestBody);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    fullUrl,
                    requestBody,
                    Map.class
            );

            log.debug("Gemini API Response: {}", response.getBody());

            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> responseParts = (List<Map<String, Object>>) content.get("parts");
                    String text = (String) responseParts.get(0).get("text");
                    return text.trim();
                }
            }

            throw new RuntimeException("Empty response from Gemini API");
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: ", e);
            throw new RuntimeException("Gemini API call failed", e);
        }
    }
}

