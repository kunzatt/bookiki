package com.corp.bookiki.notice;

import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

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
    void findByDeletedFalse() {
        // given
        NoticeEntity notice1 = NoticeEntity.builder()
                .title("Title 1")
                .content("Content 1")
                .build();

        // deleted 필드를 ReflectionTestUtils로 설정
        ReflectionTestUtils.setField(notice1, "deleted", false);
        noticeRepository.save(notice1);

        NoticeEntity notice2 = NoticeEntity.builder()
                .title("Title 2")
                .content("Content 2")
                .build();

        // deleted 필드를 ReflectionTestUtils로 설정
        ReflectionTestUtils.setField(notice2, "deleted", true);
        noticeRepository.save(notice2);

        // when
        List<NoticeEntity> result = noticeRepository.findByDeletedFalseOrderByCreatedAtDesc();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Title 1");
    }

    @Test
    void findByDeletedFalseAndTitleContainingOrContentContaining() {
        // given
        NoticeEntity notice1 = NoticeEntity.builder()
                .title("Important Notice")
                .content("Test Content")
                .build();
        NoticeEntity notice2 = NoticeEntity.builder()
                .title("General Notice")
                .content("Important Content")
                .build();
        ReflectionTestUtils.setField(notice1, "deleted", false);
        ReflectionTestUtils.setField(notice2, "deleted", false);
        noticeRepository.saveAll(Arrays.asList(notice1, notice2));

        // when
        List<NoticeEntity> result = noticeRepository.findByDeletedFalseAndTitleContainingOrContentContainingOrderByCreatedAtDesc("Important", "Important");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("title")
                .containsExactlyInAnyOrder("Important Notice", "General Notice");
    }
}