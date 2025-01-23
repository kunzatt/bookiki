package com.corp.bookiki.notice;

import com.corp.bookiki.global.error.exception.NoticeException;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import com.corp.bookiki.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Slf4j
class NoticeServiceTest {
    @InjectMocks
    private NoticeService noticeService;

    @Mock
    private NoticeRepository noticeRepository;

    @Nested
    @DisplayName("공지사항 생성 테스트")
    class CreateNotice {
        @Test
        void createNotice_WhenValidRequest_ThenSuccess() {
            // given
            NoticeRequest request = new NoticeRequest();
            request.setTitle("Test Notice");
            request.setContent("Test Content");
            log.info("공지사항 생성 요청: {}", request.getTitle());

            given(noticeRepository.save(any(NoticeEntity.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when & then
            assertDoesNotThrow(() -> {
                noticeService.createNotice(request);
                log.info("공지사항 생성 성공");
            });
        }

        @Test
        void createNotice_WhenInvalidRequest_ThenThrowException() {
            // given
            NoticeRequest request = new NoticeRequest();
            log.info("유효하지 않은 공지사항 생성 요청");

            // when & then
            assertThrows(NoticeException.class, () -> {
                noticeService.createNotice(request);
                log.error("예상된 실패 발생");
            });
        }
    }

    @Nested
    @DisplayName("공지사항 조회 테스트")
    class FindNotices {
        @Test
        void findAllNotices_ReturnsActiveNotices() {
            // given
            List<NoticeEntity> notices = Arrays.asList(
                    NoticeEntity.builder()
                            .title("Notice 1")
                            .content("Content 1")
                            .build()
            );
            given(noticeRepository.findByDeletedFalse()).willReturn(notices);
            log.info("Mock 설정 완료: 활성 공지사항 {}건", notices.size());

            // when
            List<NoticeEntity> result = noticeService.findAllNotices();

            // then
            assertThat(result).hasSize(1);
            log.info("공지사항 조회 성공: {}건", result.size());
        }
    }
}