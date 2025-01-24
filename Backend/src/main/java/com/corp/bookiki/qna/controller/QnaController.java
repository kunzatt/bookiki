package com.corp.bookiki.qna.controller;

import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.qna.service.QnaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
@Tag(name = "문의사항 API", description = "문의사항 관련 API")
@Slf4j
public class QnaController {
    private final QnaService qnaService;

    @PostMapping("")
    public ResponseEntity<Integer> createQna(@Valid @RequestBody NoticeRequest request) {
        log.info("문의사항 등록: {}", request.getTitle());

        // user API 완성 후 등록된 문의사항의 id 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(0);
    }

}
