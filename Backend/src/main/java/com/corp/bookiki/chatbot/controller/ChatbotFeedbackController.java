package com.corp.bookiki.chatbot.controller;

import com.corp.bookiki.chatbot.dto.ChatbotFeedbackResponse;
import com.corp.bookiki.chatbot.dto.UpdateFeedbackStatusRequest;
import com.corp.bookiki.chatbot.entity.FeedbackStatus;
import com.corp.bookiki.chatbot.service.ChatbotFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
@Tag(name = "챗봇 피드백 API", description = "관리자용 챗봇 피드백 관련 API")
@Slf4j
public class ChatbotFeedbackController {

    private final ChatbotFeedbackService chatbotFeedbackService;

    @GetMapping("")
    @Operation(summary = "피드백 목록 조회", description = "피드백 목록을 필터링하여 조회합니다.")
    public ResponseEntity<Page<ChatbotFeedbackResponse>> selectFeedbacks(
            @Parameter(description = "피드백 상태", example = "PENDING")
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ChatbotFeedbackResponse> feedbacks = chatbotFeedbackService.selectAllFeedbacks(
                status, category, startDate, endDate, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(feedbacks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 피드백 조회", description = "ID로 특정 피드백을 조회합니다.")
    public ResponseEntity<ChatbotFeedbackResponse> selectFeedbackById(@PathVariable int id) {
        ChatbotFeedbackResponse feedback = chatbotFeedbackService.selectFeedbackById(id);
        return ResponseEntity.status(HttpStatus.OK).body(feedback);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "피드백 상태 업데이트", description = "피드백의 처리 상태를 업데이트합니다.")
    public ResponseEntity<ChatbotFeedbackResponse> updateFeedbackStatus(
            @PathVariable int id,
            @RequestBody @Valid UpdateFeedbackStatusRequest request) {

        ChatbotFeedbackResponse updated = chatbotFeedbackService.updateFeedbackStatus(id, request.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 피드백 조회", description = "특정 사용자의 피드백 내역을 조회합니다.")
    public ResponseEntity<List<ChatbotFeedbackResponse>> selectFeedbackByUserId(@PathVariable int userId) {
        List<ChatbotFeedbackResponse> feedbacks = chatbotFeedbackService.selectFeedbackByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(feedbacks);
    }

}
