package com.corp.bookiki.qna.controller;

import com.corp.bookiki.qna.dto.QnaDetailResponse;
import com.corp.bookiki.qna.dto.QnaListResponse;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.service.QnaService;
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
@RequestMapping("/qna")
@RequiredArgsConstructor
@Tag(name = "문의사항 API", description = "문의사항 관련 API")
@Slf4j
public class QnaController {
    // 개발용 임시 상수
    private static final int TEST_AUTHOR_ID = 1;
    private static final String TEST_AUTHOR_NAME = "박성문";

    private final QnaService qnaService;

    @PostMapping("")
    public ResponseEntity<Integer> createQna(@Valid @RequestBody QnaRequest request) {
        log.info("문의사항 등록: {}", request.getTitle());
        int qnaId = qnaService.creatQna(request, TEST_AUTHOR_ID);

        // user API 완성 후 등록된 문의사항의 id 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(qnaId);
    }

    @GetMapping("")
    public ResponseEntity<List<QnaListResponse>> selectAllQnas(@RequestParam(required = false) String keyword) {
        log.info("문의사항 목록 조회");
        List<QnaEntity> qnas = qnaService.selectAllQnas(keyword);
        List<QnaListResponse> responses = new ArrayList<>();
        for (QnaEntity qnaEntity : qnas) {
            responses.add(new QnaListResponse(qnaEntity));
        }
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QnaDetailResponse> selectQnaById(@PathVariable int id) {
        log.info("문의사항 상세 조회: id={}", id);
        QnaDetailResponse qnaResponse = qnaService.selectQnaByIdWithComment(id);
        return ResponseEntity.status(HttpStatus.OK).body(qnaResponse);
    }


}
