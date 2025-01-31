package com.corp.bookiki.qna.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
@Tag(name = "문의사항 API", description = "문의사항 관련 API")
@Slf4j
public class QnaCommentController {
    // 개발용 임시 상수
    private static final int TEST_AUTHOR_ID = 9;
}

