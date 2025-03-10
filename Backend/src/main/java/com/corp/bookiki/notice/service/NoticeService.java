package com.corp.bookiki.notice.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.NoticeException;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeUpdate;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {
    private final NoticeRepository noticeRepository;

    // 공지사항 등록 -> 공지사항 id 반환
    @Transactional
    public int createNotice(NoticeRequest request) {
        try {
            validateNoticeInput(request);

            NoticeEntity noticeEntity = noticeRepository.save(request.toEntity());
            return noticeEntity.getId();
        } catch (Exception ex) {
            log.error("공지사항 등록 실패: {}", ex.getMessage());
            throw new NoticeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 공지사항 조회 (삭제되지 않은)
    @Transactional(readOnly = true)
    public Page<NoticeEntity> selectAllNotices(Pageable pageable) {
        try {
            return noticeRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);
        } catch (Exception ex) {
            log.error("공지사항 목록 조회 실패: {}", ex.getMessage());
            throw new NoticeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 공지사항 검색
    @Transactional(readOnly = true)
    public Page<NoticeEntity> searchNotice(String keyword, Pageable pageable) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return selectAllNotices(pageable);
            }
            return noticeRepository.findBySearchCriteria(keyword, pageable);
        } catch (Exception ex) {
            log.error("공지사항 검색 실패: {}", ex.getMessage());
            throw new NoticeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 공지사항 1개 조회
    @Transactional
    public NoticeEntity selectNoticeById(int id) {
        try {
            NoticeEntity notice = noticeRepository.getReferenceById(id);
            if (notice == null || notice.isDeleted()) {
                throw new NoticeException(ErrorCode.NOTICE_NOT_FOUND);
            }

            notice.incrementViewCount();
            noticeRepository.save(notice);

            return notice;
        } catch (EntityNotFoundException e) {
            throw new NoticeException(ErrorCode.NOTICE_NOT_FOUND);
        } catch (Exception ex) {
            log.error("공지사항 조회 실패: {}", ex.getMessage());
            throw new NoticeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(int id) {
        try {
            NoticeEntity notice = selectNoticeById(id);
            notice.delete();
        } catch (EntityNotFoundException e) {
            log.error("공지사항 삭제 실패 - 존재하지 않는 ID: {}", id, e);
            throw new NoticeException(ErrorCode.NOTICE_NOT_FOUND);
        } catch (Exception e) {
            log.error("공지사항 삭제 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
            throw new NoticeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 공지사항 수정
    @Transactional
    public void updateNotice(NoticeUpdate update) {
        try {
            NoticeEntity notice = selectNoticeById(update.getId());
            notice.update(update.getTitle(), update.getContent());
        } catch (EntityNotFoundException e) {
            log.error("공지사항 수정 실패 - 존재하지 않는 ID: {}", update.getId(), e);
            throw new NoticeException(ErrorCode.NOTICE_NOT_FOUND);
        } catch (Exception e) {
            log.error("공지사항 수정 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
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
