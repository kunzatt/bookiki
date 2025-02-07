package com.corp.bookiki.chatbot.controller;

import com.corp.bookiki.chatbot.dto.ChatbotFeedbackResponse;
import com.corp.bookiki.chatbot.dto.UpdateFeedbackStatusRequest;
import com.corp.bookiki.chatbot.entity.FeedbackStatus;
import com.corp.bookiki.chatbot.service.ChatbotFeedbackService;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "피드백 목록 조회", description = "피드백 목록을 필터링하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2024-02-07T10:00:00",
                                                "status": 500,
                                                "message": "서버 오류가 발생했습니다",
                                                "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<Page<ChatbotFeedbackResponse>> selectFeedbacks(
            @Parameter(description = "피드백 상태", example = "PENDING")
            @RequestParam(required = false) FeedbackStatus status,
            @Parameter(description = "피드백 카테고리", example = "QR_ERROR")
            @RequestParam(required = false) String category,
            @Parameter(description = "조회 시작일", example = "2024-02-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료일", example = "2024-02-07T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "페이지네이션 정보", example = "size=20&page=0&sort=id,desc")
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("피드백 목록 조회: status={}, category={}", status, category);
        Page<ChatbotFeedbackResponse> feedbacks = chatbotFeedbackService.selectAllFeedbacks(
                status, category, startDate, endDate, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(feedbacks);
    }

    @Operation(summary = "특정 피드백 조회", description = "ID로 특정 피드백을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatbotFeedbackResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "피드백을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2024-02-07T10:00:00",
                                                "status": 404,
                                                "message": "피드백을 찾을 수 없습니다",
                                                "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ChatbotFeedbackResponse> selectFeedbackById(
            @Parameter(description = "피드백 ID", required = true, example = "1")
            @PathVariable int id
    ) {
        log.info("피드백 상세 조회: id={}", id);
        ChatbotFeedbackResponse feedback = chatbotFeedbackService.selectFeedbackById(id);
        return ResponseEntity.status(HttpStatus.OK).body(feedback);
    }

    @Operation(summary = "피드백 상태 업데이트", description = "피드백의 처리 상태를 업데이트합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상태 업데이트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatbotFeedbackResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 상태값",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2024-02-07T10:00:00",
                                                "status": 400,
                                                "message": "잘못된 상태값입니다",
                                                "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "피드백을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2024-02-07T10:00:00",
                                                "status": 404,
                                                "message": "피드백을 찾을 수 없습니다",
                                                "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ChatbotFeedbackResponse> updateFeedbackStatus(
            @Parameter(description = "피드백 ID", required = true, example = "1")
            @PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 상태 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateFeedbackStatusRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "status": "IN_PROGRESS"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid UpdateFeedbackStatusRequest request
    ) {
        log.info("피드백 상태 업데이트: id={}, status={}", id, request.getStatus());
        ChatbotFeedbackResponse updated = chatbotFeedbackService.updateFeedbackStatus(id, request.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @Operation(summary = "사용자별 피드백 조회", description = "특정 사용자의 피드백 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ChatbotFeedbackResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2024-02-07T10:00:00",
                                                "status": 500,
                                                "message": "서버 오류가 발생했습니다",
                                                "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatbotFeedbackResponse>> selectFeedbackByUserId(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable int userId
    ) {
        log.info("사용자별 피드백 조회: userId={}", userId);
        List<ChatbotFeedbackResponse> feedbacks = chatbotFeedbackService.selectFeedbackByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(feedbacks);
    }

}
