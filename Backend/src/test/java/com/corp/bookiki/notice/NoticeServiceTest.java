package com.corp.bookiki.notice;

import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeUpdate;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import com.corp.bookiki.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        List<NoticeEntity> notices = Arrays.asList(
                NoticeEntity.builder().title("Notice 1").content("Content 1").build(),
                NoticeEntity.builder().title("Notice 2").content("Content 2").build()
        );

        when(noticeRepository.findByDeletedFalseOrderByCreatedAtDesc()).thenReturn(notices);

        // when
        List<NoticeEntity> result = noticeService.selectAllNotices();

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Notice 1");
        assertThat(result.get(0).isDeleted()).isFalse();
    }

    @Test
    void searchNotice() {
        // given
        String keyword = "Important";
        NoticeEntity notice = NoticeEntity.builder()
                .title("Important Notice")
                .content("Test Content")
                .build();
        given(noticeRepository.findByDeletedFalseAndTitleContainingOrContentContainingOrderByCreatedAtDesc(keyword, keyword))
                .willReturn(Collections.singletonList(notice));

        // when
        List<NoticeEntity> result = noticeService.searchNotice(keyword);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Important Notice");
        verify(noticeRepository).findByDeletedFalseAndTitleContainingOrContentContainingOrderByCreatedAtDesc(keyword, keyword);
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

        // ID를 직접 설정 (ReflectionTestUtils를 활용)
        ReflectionTestUtils.setField(entity, "id", noticeId);

        when(noticeRepository.getReferenceById(noticeId)).thenReturn(entity);

        // when
        NoticeEntity result = noticeService.selectNoticeById(noticeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Notice");
    }

    @DisplayName("공지사항 삭제 테스트")
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

    @DisplayName("공지사항 수정 테스트")
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
}
