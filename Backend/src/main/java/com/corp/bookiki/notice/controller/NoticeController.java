package com.corp.bookiki.notice.controller;

import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeResponse;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.service.NoticeService;
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
@Slf4j
public class NoticeController {
    private final NoticeService noticeService;

    // 공지사항 등록
    @PostMapping("/admin/notices")
    public ResponseEntity<String> createNotice(@Valid @RequestBody NoticeRequest request) {
        log.info("공지사항 등록: {}", request.getTitle());
        noticeService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("공지사항이 등록되었습니다.");
    }

    // 공지사항 수정
    @PutMapping("/admin/notices/{id}")
    public ResponseEntity<String> updateNotice(@PathVariable int id, @Valid @RequestBody NoticeRequest request) {
        log.info("공지사항 수정: id={}, title={}", id, request.getTitle());
        noticeService.updateNotice(id, request);
        return ResponseEntity.status(HttpStatus.OK).body("공지사항이 수정되었습니다.");
    }

    // 공지사항 전체 목록
    @GetMapping("/notices")
    public ResponseEntity<List<NoticeResponse>> getNotices() {
        log.info("공지사항 목록 조회");
        List<NoticeEntity> notices = noticeService.findAllNotices();
        List<NoticeResponse> responses = new ArrayList<>();
        for (NoticeEntity notice : notices) {
            responses.add(new NoticeResponse(notice));
        }
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    // 공지사항 상세 조회
    @GetMapping("/notices/{id}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable int id) {
        log.info("공지사항 상세 조회: id={}", id);
        NoticeEntity notice = noticeService.findNoticeById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new NoticeResponse(notice));
    }

    // 공지사항 삭제
    @DeleteMapping("/admin/notices/{id}")
    public ResponseEntity<String> deleteNotice(@PathVariable int id) {
        log.info("공지사항 삭제: id={}", id);
        noticeService.deleteNotice(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("공지사항이 삭제되었습니다.");
    }
}
