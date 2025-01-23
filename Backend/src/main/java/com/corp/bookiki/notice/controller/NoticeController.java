package com.corp.bookiki.notice.controller;

import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeResponse;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.service.NoticeService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "공지사항 관리", description = "공지사항 CRUD 관련 API")
@Slf4j
public class NoticeController {
    private final NoticeService noticeService;

    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "공지사항 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "\"공지사항이 등록되었습니다.\"")
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
                                                "timestamp": "2024-01-23T10:00:00",
                                                "status": 400,
                                                "message": "잘못된 입력값입니다",
                                                "errors": [
                                                    {
                                                        "field": "title",
                                                        "value": "",
                                                        "reason": "제목은 필수 입력값입니다."
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/admin/notices")
    public ResponseEntity<String> createNotice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공지사항 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NoticeRequest.class))
            )
            @Valid @RequestBody NoticeRequest request
    ) {
        log.info("공지사항 등록: {}", request.getTitle());
        noticeService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("공지사항이 등록되었습니다.");
    }

    @Operation(summary = "공지사항 수정", description = "기존 공지사항의 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "\"공지사항이 수정되었습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공지사항을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "timestamp": "2025-01-23T08:45:00.000Z",
                                        "status": 404,
                                        "message": "공지사항을 찾을 수 없습니다.",
                                        "errors": []
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "timestamp": "2025-01-23T08:45:00.000Z",
                                        "status": 400,
                                        "message": "유효하지 않은 요청 데이터입니다.",
                                        "errors": [
                                            {
                                                "field": "title",
                                                "value": null,
                                                "reason": "제목은 필수입니다."
                                            }
                                        ]
                                    }
                                    """)
                    )
            )
    })
    @PutMapping("/admin/notices/{id}")
    public ResponseEntity<String> updateNotice(
            @Parameter(description = "공지사항 ID", required = true, example = "1")
            @PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 공지사항 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NoticeRequest.class))
            )
            @Valid @RequestBody NoticeRequest request
    ) {
        log.info("공지사항 수정: id={}, title={}", id, request.getTitle());
        noticeService.updateNotice(id, request);
        return ResponseEntity.ok("공지사항이 수정되었습니다.");
    }

    @Operation(summary = "공지사항 목록 조회", description = "삭제되지 않은 모든 공지사항을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoticeResponse.class))
                    )
            )
    })
    @GetMapping("/notices")
    public ResponseEntity<List<NoticeResponse>> getNotices() {
        log.info("공지사항 목록 조회");
        List<NoticeEntity> notices = noticeService.findAllNotices();
        List<NoticeResponse> responses = new ArrayList<>();
        for (NoticeEntity notice : notices) {
            responses.add(new NoticeResponse(notice));
        }
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 내용을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoticeResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공지사항을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "timestamp": "2025-01-23T08:26:17.184Z",
                                        "status": 404,
                                        "message": "공지사항을 찾을 수 없습니다.",
                                        "errors": []
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/notices/{id}")
    public ResponseEntity<NoticeResponse> getNotice(
            @Parameter(description = "공지사항 ID", required = true, example = "1")
            @PathVariable int id
    ) {
        log.info("공지사항 상세 조회: id={}", id);
        NoticeEntity notice = noticeService.findNoticeById(id);
        return ResponseEntity.ok(new NoticeResponse(notice));
    }

    @Operation(summary = "공지사항 삭제", description = "공지사항을 소프트 삭제 처리합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "공지사항 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공지사항을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "timestamp": "2025-01-23T08:55:00.000Z",
                                        "status": 404,
                                        "message": "공지사항을 찾을 수 없습니다.",
                                        "errors": []
                                    }
                                    """)
                    )
            )
    })
    @DeleteMapping("/admin/notices/{id}")
    public ResponseEntity<Void> deleteNotice(
            @Parameter(description = "공지사항 ID", required = true, example = "1")
            @PathVariable int id
    ) {
        log.info("공지사항 삭제: id={}", id);
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
