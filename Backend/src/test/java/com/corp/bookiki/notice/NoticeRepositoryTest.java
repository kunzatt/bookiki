package com.corp.bookiki.notice;

import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class NoticeRepositoryTest {
    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    void findByDeletedFalse_ReturnsOnlyNonDeletedNotices() {
        // given
        NoticeEntity activeNotice = NoticeEntity.builder()
                .title("Active Notice")
                .content("Active Content")
                .build();

        NoticeEntity deletedNotice = NoticeEntity.builder()
                .title("Deleted Notice")
                .content("Deleted Content")
                .build();
        deletedNotice.delete();

        noticeRepository.saveAll(Arrays.asList(activeNotice, deletedNotice));
        log.info("테스트 공지사항 저장 완료: active={}, deleted={}", activeNotice.getId(), deletedNotice.getId());

        // when
        List<NoticeEntity> result = noticeRepository.findByDeletedFalse();
        log.info("삭제되지 않은 공지사항 조회 결과: count={}", result.size());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Active Notice");
    }

    @Test
    void findByDeletedFalseAndTitleContaining_ReturnsMatchingNotices() {
        // given
        NoticeEntity notice1 = NoticeEntity.builder()
                .title("Notice Test")
                .content("Content 1")
                .build();

        NoticeEntity notice2 = NoticeEntity.builder()
                .title("Different Title")
                .content("Content 2")
                .build();

        noticeRepository.saveAll(Arrays.asList(notice1, notice2));
        log.info("테스트 공지사항 저장 완료");

        // when
        List<NoticeEntity> result = noticeRepository.findByDeletedFalseAndTitleContaining("Notice");
        log.info("제목 검색 결과: count={}", result.size());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Notice");
    }
}