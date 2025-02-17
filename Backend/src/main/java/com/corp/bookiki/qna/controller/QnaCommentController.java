package com.corp.bookiki.qna.controller;

import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.service.QnaCommentService;
import com.corp.bookiki.user.dto.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/qna")
@RequiredArgsConstructor
@Tag(name = "문의사항 답변 API", description = "관리자 문의사항 답변 관련 API")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class QnaCommentController {

    private final QnaCommentService qnaCommentService;

    // 문의사항 답변 등록
    @Operation(summary = "문의사항 답변 등록", description = "관리자가 문의사항에 대한 답변을 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "답변 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "integer"),
                            examples = @ExampleObject(value = "1")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 입력값",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2024-01-24T10:00:00",
                                                "status": 400,
                                                "message": "잘못된 입력값입니다",
                                                "errors": [
                                                    {
                                                        "field": "content",
                                                        "value": "",
                                                        "reason": "답변 내용은 필수 입력값입니다"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 문의사항",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("")
    public ResponseEntity<Integer> createQnaComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "문의사항 답변 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = QnaCommentRequest.class))
            )
            @Valid @RequestBody QnaCommentRequest request,
            @CurrentUser AuthUser authUser
    ) {
        log.info("문의사항 답변 등록: {}", request.getContent());
        int qnaCommentId = qnaCommentService.createQnaComment(request, authUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(qnaCommentId);
    }

    // 문의사항 답변 삭제
    @Operation(summary = "문의사항 답변 삭제", description = "관리자가 문의사항 답변을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "답변 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 답변",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2024-01-24T10:00:00",
                                                "status": 404,
                                                "message": "댓글을 찾을 수 없습니다",
                                                "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQnaComment(
            @Parameter(description = "답변 ID", required = true, example = "1")
            @PathVariable Integer id) {
        log.info("문의사항 삭제: id={}", id);
        qnaCommentService.deleteQnaComment(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("문의사항 답변이 삭제되었습니다.");
    }

    // 문의사항 답변 수정
    @Operation(summary = "문의사항 답변 수정", description = "관리자가 문의사항 답변을 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "답변 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "\"문의사항 답변이 수정되었습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 입력값",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 답변",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("")
    public ResponseEntity<String> updateQnaComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "문의사항 답변 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = QnaCommentUpdate.class))
            )
            @Valid @RequestBody QnaCommentUpdate update
    ) {
        log.info("문의사항 답변 수정: id={}", update.getId());
        qnaCommentService.updateQnaComment(update);
        return ResponseEntity.status(HttpStatus.OK).body("문의사항 답변이 수정되었습니다.");
    }

}
