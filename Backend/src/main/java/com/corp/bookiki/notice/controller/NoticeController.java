package com.corp.bookiki.notice.controller;

import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeResponse;
import com.corp.bookiki.notice.dto.NoticeUpdate;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.service.NoticeService;
import com.corp.bookiki.user.dto.AuthUser;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "공지사항 API", description = "공지사항 관련 API")
@Slf4j
public class NoticeController {
    private final NoticeService noticeService;

    // 공지사항 등록 -> 해당 공지의 id 반환
    @PostMapping("/admin/notices")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공지사항 등록", description = "관리자가 새로운 공지사항을 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "공지사항 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "integer"),
                            examples = @ExampleObject(value = "1")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 입력값",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Integer> createNotice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공지사항 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NoticeRequest.class))
            )
            @Valid @RequestBody NoticeRequest request,
            @CurrentUser AuthUser authUser) {
        log.info("공지사항 등록: {}", request.getTitle());
        int noticeId = noticeService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(noticeId);
    }

    // 공지사항 전체 목록
    @GetMapping("")
    @Operation(summary = "공지사항 목록 조회", description = "전체 공지사항 목록을 조회하거나 키워드로 검색합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = NoticeResponse.class))
            )
    )
    public ResponseEntity<Page<NoticeResponse>> selectAllNotices(
            @Parameter(description = "검색 키워드", example = "점검")
            @RequestParam(required = false) String keyword,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("공지사항 목록 조회 - 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<NoticeEntity> noticePage = (keyword == null)
                ? noticeService.selectAllNotices(pageable)
                : noticeService.searchNotice(keyword, pageable);

        List<NoticeResponse> responses = new ArrayList<>();
        for (NoticeEntity notice : noticePage.getContent()) {
            responses.add(new NoticeResponse(notice));
        }

        Page<NoticeResponse> responsePage = new PageImpl<>(
                responses,
                pageable,
                noticePage.getTotalElements()
        );

        return ResponseEntity.status(HttpStatus.OK).body(responsePage);
    }

    // 공지사항 상세 조회
    @GetMapping("/notices/{id}")
    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 내용을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoticeResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 공지사항",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2024-01-24T10:00:00",
                                                "status": 404,
                                                "message": "존재하지 않는 공지사항입니다",
                                                "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<NoticeResponse> selectNoticeById(
            @Parameter(description = "공지사항 ID", required = true, example = "1")
            @PathVariable int id) {
        log.info("공지사항 상세 조회: id={}", id);
        NoticeEntity notice = noticeService.selectNoticeById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new NoticeResponse(notice));
    }

    // 공지사항 삭제
    @DeleteMapping("/admin/notices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공지사항 삭제", description = "관리자가 특정 공지사항을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 공지사항",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<String> deleteNotice(
            @Parameter(description = "공지사항 ID", required = true, example = "1")
            @PathVariable int id,
            @CurrentUser AuthUser authUser) {
        log.info("공지사항 삭제: id={}", id);
        noticeService.deleteNotice(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("공지사항이 삭제되었습니다.");
    }

    // 공지사항 수정
    @PutMapping("/admin/notices")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공지사항 수정", description = "관리자가 공지사항을 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "\"공지사항이 수정되었습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 입력값",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 공지사항",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<String> updateNotice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공지사항 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NoticeUpdate.class))
            )
            @Valid @RequestBody NoticeUpdate update,
            @CurrentUser AuthUser authUser) {
        log.info("공지사항 수정: id={}, title={}", update.getId(), update.getTitle());
        noticeService.updateNotice(update);
        return ResponseEntity.status(HttpStatus.OK).body("공지사항이 수정되었습니다.");
    }
}
