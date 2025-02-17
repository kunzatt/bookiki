package com.corp.bookiki.notice;

import com.corp.bookiki.global.error.exception.NoticeException;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeUpdate;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import com.corp.bookiki.notice.service.NoticeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeService noticeService; // Mock 주입

    @BeforeEach
    void setUp() {
        Mockito.reset(noticeRepository);
    }

    @DisplayName("공지사항 등록 테스트")
    @Test
    void createNotice() {
        // given
        NoticeRequest request = new NoticeRequest("New Notice", "Content");
        NoticeEntity entity = NoticeEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        ReflectionTestUtils.setField(entity, "id", 1);

        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(entity);

        // when
        int noticeId = noticeService.createNotice(request);

        // then
        assertThat(noticeId).isEqualTo(entity.getId());
        verify(noticeRepository, times(1)).save(any(NoticeEntity.class));
    }

    @DisplayName("공지사항 전체 목록 조회 테스트")
    @Test
    void selectAllNotices() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<NoticeEntity> notices = Arrays.asList(
                NoticeEntity.builder().title("Notice 1").content("Content 1").build(),
                NoticeEntity.builder().title("Notice 2").content("Content 2").build()
        );
        Page<NoticeEntity> noticePage = new PageImpl<>(notices, pageable, notices.size());

        when(noticeRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable)).thenReturn(noticePage);

        // when
        Page<NoticeEntity> result = noticeService.selectAllNotices(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Notice 1");
        assertThat(result.getContent().get(0).isDeleted()).isFalse();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("키워드로 공지사항 검색")
    void searchNotice_WithKeyword() {
        // given
        String keyword = "Important";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        NoticeEntity notice = NoticeEntity.builder()
                .title("Important Notice")
                .content("Test Content")
                .build();
        List<NoticeEntity> notices = Collections.singletonList(notice);
        Page<NoticeEntity> noticePage = new PageImpl<>(notices, pageable, notices.size());

        given(noticeRepository.findBySearchCriteria(keyword, pageable)).willReturn(noticePage);

        // when
        Page<NoticeEntity> result = noticeService.searchNotice(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Important Notice");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @DisplayName("공지사항 상세 조회 테스트")
    @Test
    void selectNoticeById() {
        // given
        int noticeId = 1;
        NoticeEntity entity = NoticeEntity.builder()
                .title("Notice")
                .content("Content")
                .build();
        ReflectionTestUtils.setField(entity, "id", noticeId);
        when(noticeRepository.getReferenceById(noticeId)).thenReturn(entity);

        // when
        NoticeEntity result = noticeService.selectNoticeById(noticeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Notice");
        assertThat(result.getViewCount()).isEqualTo(1); // viewCount 증가 확인
    }

    @DisplayName("공지사항 삭제 테스트 - 정상 삭제")
    @Test
    void deleteNotice() {
        // given
        int noticeId = 1;
        NoticeEntity entity = NoticeEntity.builder()
                .title("Notice")
                .content("Content")
                .build();

        ReflectionTestUtils.setField(entity, "id", noticeId);

        when(noticeRepository.getReferenceById(noticeId)).thenReturn(entity);

        // when
        noticeService.deleteNotice(noticeId);

        // then
        assertThat(entity.isDeleted()).isTrue();
    }

    @DisplayName("공지사항 수정 테스트 - 정상 수정")
    @Test
    void updateNotice() {
        // given
        int noticeId = 1;
        NoticeEntity entity = NoticeEntity.builder()
                .title("Old Title")
                .content("Old Content")
                .build();

        ReflectionTestUtils.setField(entity, "id", noticeId);

        NoticeUpdate update = NoticeUpdate.builder()
                .id(noticeId)
                .title("New Title")
                .content("New Content")
                .build();

        when(noticeRepository.getReferenceById(noticeId)).thenReturn(entity);

        // when
        noticeService.updateNotice(update);

        // then
        assertThat(entity.getTitle()).isEqualTo("New Title");
        assertThat(entity.getContent()).isEqualTo("New Content");
    }

    @DisplayName("존재하지 않는 공지사항 조회 시 예외 발생")
    @Test
    void selectNoticeById_NotFound() {
        // given
        int noticeId = 999;
        when(noticeRepository.getReferenceById(noticeId))
                .thenThrow(new EntityNotFoundException());

        // when & then
        try {
            noticeService.selectNoticeById(noticeId);
            fail("예외가 발생해야 합니다.");
        } catch (NoticeException e) {
            assertThat(e).isInstanceOf(NoticeException.class);
        }
    }
}
