package com.corp.bookiki.notice;

import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    @DisplayName("삭제되지 않은 공지사항 페이지 조회")
    void findByDeletedFalse() {
        // given
        NoticeEntity notice1 = NoticeEntity.builder()
                .title("Title 1")
                .content("Content 1")
                .build();
        ReflectionTestUtils.setField(notice1, "deleted", false);
        noticeRepository.save(notice1);

        NoticeEntity notice2 = NoticeEntity.builder()
                .title("Title 2")
                .content("Content 2")
                .build();
        ReflectionTestUtils.setField(notice2, "deleted", true);
        noticeRepository.save(notice2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<NoticeEntity> result = noticeRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);

        // then
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Title 1");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

//    @Test
//    @DisplayName("키워드로 공지사항 검색")
//    void findBySearchCriteria() {
//        // given
//        NoticeEntity notice1 = NoticeEntity.builder()
//                .title("Important Notice")
//                .content("Test Content")
//                .build();
//        NoticeEntity notice2 = NoticeEntity.builder()
//                .title("General Notice")
//                .content("Important Content")
//                .build();
//
//        // boolean 값 직접 설정
//        notice1.update(notice1.getTitle(), notice1.getContent());  // deleted는 false로 유지
//        notice2.update(notice2.getTitle(), notice2.getContent());  // deleted는 false로 유지
//
//        noticeRepository.saveAll(Arrays.asList(notice1, notice2));
//
//        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
//
//        // when
//        Page<NoticeEntity> result = noticeRepository.findBySearchCriteria("Important", pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(2);
//        assertThat(result.getTotalElements()).isEqualTo(2);
//    }
}