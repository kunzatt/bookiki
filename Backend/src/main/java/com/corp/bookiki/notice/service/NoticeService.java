package com.corp.bookiki.notice.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.NoticeException;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {
    private final NoticeRepository noticeRepository;

    // 공지사항 등록
    @Transactional
    public void createNotice(NoticeRequest request) {
        try {
            validateNoticeInput(request);

            NoticeEntity notice = NoticeEntity.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .build();

            noticeRepository.save(notice);
        } catch (Exception ex) {
            log.error("공지사항 등록 실패: {}", ex.getMessage());
            throw new NoticeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 공지사항 수정
    @Transactional
    public void updateNotice(int id, NoticeRequest request) {
        validateNoticeInput(request);

        NoticeEntity notice = findNoticeById(id);
        notice.updateTitle(request.getTitle());
        notice.updateContent(request.getContent());
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(int id) {
        NoticeEntity notice = findNoticeById(id);
        notice.delete();
    }

    // 공지사항 조회 (삭제되지 않은)
    @Transactional
    public List<NoticeEntity> findAllNotices() {
        try {
            return noticeRepository.findByDeletedFalse();
        } catch (Exception ex) {
            log.error("공지사항 목록 조회 실패: {}", ex.getMessage());
            throw new NoticeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public NoticeEntity findNoticeById(int id) {
        try {
            NoticeEntity notice = noticeRepository.getReferenceById(id);
            if (notice == null) {
                throw new NoticeException(ErrorCode.NOTICE_NOT_FOUND);
            }
            return notice;
        } catch (Exception ex) {
            log.error("공지사항 조회 실패: {}", ex.getMessage());
            throw new NoticeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 제목이나 본문이 빈칸이면 유효하지 않은 값
    private void validateNoticeInput(NoticeRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new NoticeException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new NoticeException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
