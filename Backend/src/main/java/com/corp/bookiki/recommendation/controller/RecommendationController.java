package com.corp.bookiki.recommendation.controller;

import com.corp.bookiki.bookinformation.dto.BookInformationResponse;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.recommendation.dto.RecommendationRequest;
import com.corp.bookiki.recommendation.dto.RecommendationResponse;
import com.corp.bookiki.recommendation.service.GeminiService;
import com.corp.bookiki.recommendation.service.RecommendationService;
import com.corp.bookiki.user.dto.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "도서 추천 API", description = "도서 추천 관련 API")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("")
    @Operation(summary = "도서 추천", description = "최근 한 달간의 대출 기록을 바탕으로 도서를 추천합니다.")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @CurrentUser AuthUser authUser
    ) {
        return ResponseEntity.ok(
                recommendationService.getRecommendations(authUser.getId(), new RecommendationRequest())
        );
    }
}
