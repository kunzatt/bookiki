package com.corp.bookiki.recommendation.service;

import com.corp.bookiki.bookinformation.service.BookInformationService;
import com.corp.bookiki.bookitem.enums.SearchType;
import com.corp.bookiki.global.error.exception.BookItemException;
import com.corp.bookiki.recommendation.dto.RecommendedBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookitem.dto.BookItemListResponse;
import com.corp.bookiki.recommendation.dto.RecommendationRequest;
import com.corp.bookiki.recommendation.dto.RecommendationResponse;
import com.corp.bookiki.recommendation.dto.RecommendedBook;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {
    private final BookItemService bookItemService;
    private final GeminiService geminiService;
    private final BookHistoryService bookHistoryService;

    public RecommendationResponse getRecommendations(Integer userId, RecommendationRequest request) {
        try {
            return getKeywordBasedRecommendations(userId, request);
        } catch (BookItemException e) {
            log.warn("키워드 검색 실패 - userId={}, error={}", userId, e.getMessage());
            return getPopularBooksRecommendations(request.getLimit());
        }
    }

    @Transactional(readOnly = true)
    protected RecommendationResponse getKeywordBasedRecommendations(Integer userId, RecommendationRequest request) {
        List<BookHistoryResponse> borrowHistory = bookHistoryService.getUserBookHistories(
                userId,
                request.getStartDate(),
                request.getEndDate(),
                false,
                PageRequest.of(0, 5)
        ).getContent();

        log.info("사용자 대출 이력 조회 - userId={}, 건수={}", userId, borrowHistory.size());

        String keywords = geminiService.getRecommendationKeywords(borrowHistory);
        log.info("추출된 키워드={}", keywords);

        List<BookItemListResponse> recommendations = bookItemService.selectBooks(
                SearchType.KEYWORD,
                keywords,
                0,
                request.getLimit()
        ).getContent();

        log.info("키워드 기반 도서 검색 결과 - 건수={}", recommendations.size());
        return buildResponse(recommendations, "최근 대출 이력 기반 추천");
    }

    @Transactional(readOnly = true)
    protected RecommendationResponse getPopularBooksRecommendations(int limit) {
        List<BookItemListResponse> popularBooks = bookItemService.getPopularBooksByCategory(
                null,
                limit
        );

        log.info("인기 도서 조회 결과 - 건수={}", popularBooks.size());
        return buildResponse(popularBooks, "인기 도서 기반 추천");
    }

    private RecommendationResponse buildResponse(List<BookItemListResponse> books, String reason) {
        return RecommendationResponse.builder()
                .recommendations(books.stream()
                        .map(book -> RecommendedBook.builder()
                                .bookItemId(book.getId())
                                .title(book.getTitle())
                                .author(book.getAuthor())
                                .build())
                        .collect(Collectors.toList()))
                .recommendationReason(reason)
                .build();
    }
}