package com.corp.bookiki.qna.controller;

import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.qna.dto.QnaDetailResponse;
import com.corp.bookiki.qna.dto.QnaListResponse;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.dto.QnaUpdate;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.service.QnaService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
@Tag(name = "문의사항 API", description = "문의사항 관련 API")
@Slf4j
public class QnaController {

    private final QnaService qnaService;
    private final UserRepository userRepository;

    // 문의사항 등록
    @Operation(summary = "문의사항 등록", description = "새로운 문의사항을 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "문의사항 등록 성공",
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
                                                        "field": "title",
                                                        "value": "",
                                                        "reason": "제목은 필수 입력값입니다"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("")
    public ResponseEntity<Integer> createQna(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "문의사항 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = QnaRequest.class))
            ) @Valid @RequestBody QnaRequest request, @CurrentUser AuthUser authUser) {
        log.info("문의사항 등록: {}", request.getTitle());
        int qnaId = qnaService.createQna(request, authUser.getId());

        // 등록된 문의사항의 id 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(qnaId);
    }

    // 문의사항 목록 조회
    @Operation(summary = "문의사항 목록 조회", description = "문의사항 목록을 조회하고 필터링합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = QnaListResponse.class))
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<Page<QnaListResponse>> selectQnas(
            @Parameter(description = "검색 키워드", example = "도서관")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "문의사항 유형", example = "GENERAL")
            @RequestParam(required = false) String qnaType,
            @Parameter(description = "답변 여부", example = "true")
            @RequestParam(required = false) Boolean answered,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @CurrentUser AuthUser authUser
    ) {
        log.info("문의사항 목록 조회 - 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<QnaEntity> qnaPage = qnaService.selectQnas(keyword, qnaType, answered, authUser, pageable);
        List<QnaEntity> qnaList = qnaPage.getContent();
        List<QnaListResponse> responseList = new ArrayList<>();

        for (QnaEntity qna : qnaList) {
            try {
                UserEntity author = userRepository.getReferenceById(qna.getAuthorId());
                responseList.add(new QnaListResponse(qna, author.getUserName()));
            } catch (EntityNotFoundException ex) {
                throw new UserException(ErrorCode.USER_NOT_FOUND);
            }
        }

        Page<QnaListResponse> responsePage = new PageImpl<>(
                responseList,
                pageable,
                qnaPage.getTotalElements()
        );

        return ResponseEntity.status(HttpStatus.OK).body(responsePage);
    }

    // 문의사항 1개 상세조회
    @Operation(summary = "문의사항 상세 조회", description = "특정 문의사항의 상세 내용을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = QnaDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 문의사항",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<QnaDetailResponse> selectQnaById(
            @Parameter(description = "문의사항 ID", required = true, example = "1")
            @PathVariable int id,
            @CurrentUser AuthUser authUser
    ) {
        log.info("문의사항 상세 조회: id={}", id);
        QnaDetailResponse qnaResponse = qnaService.selectQnaByIdWithComment(id, authUser);
        return ResponseEntity.status(HttpStatus.OK).body(qnaResponse);
    }

    // 문의사항 삭제
    @Operation(summary = "문의사항 삭제", description = "특정 문의사항을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 문의사항",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQna(
            @Parameter(description = "문의사항 ID", required = true, example = "1")
            @PathVariable int id,
            @CurrentUser AuthUser authUser
    ) {
        log.info("문의사항 삭제: id={}", id);
        qnaService.deleteQna(id, authUser.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("문의사항이 삭제되었습니다.");
    }

    // 문의사항 수정
    @Operation(summary = "문의사항 수정", description = "문의사항을 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "\"문의사항이 수정되었습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 입력값",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 문의사항",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("")
    public ResponseEntity<String> updateQna(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "문의사항 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = QnaUpdate.class))
            )
            @Valid @RequestBody QnaUpdate update,
            @CurrentUser AuthUser authUser
    ) {
        log.info("문의사항 수정: id={}", update.getId());
        qnaService.updateQna(update, authUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body("문의사항이 수정되었습니다.");
    }
}
