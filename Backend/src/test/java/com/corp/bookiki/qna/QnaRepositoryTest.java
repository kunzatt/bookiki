package com.corp.bookiki.qna;

import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
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
class QnaRepositoryTest {

    @Autowired
    private QnaRepository qnaRepository;

    @Test
    @DisplayName("삭제되지 않은 문의사항 조회 테스트")
    void findByDeletedFalseOrderByCreatedAtDesc() {
        // given
        QnaEntity qna1 = QnaEntity.builder()
                .title("Title 1")
                .content("Content 1")
                .qnaType("GENERAL")
                .authorId(1)
                .build();

        QnaEntity qna2 = QnaEntity.builder()
                .title("Title 2")
                .content("Content 2")
                .qnaType("GENERAL")
                .authorId(1)
                .build();

        ReflectionTestUtils.setField(qna1, "deleted", false);
        ReflectionTestUtils.setField(qna2, "deleted", true);

        qnaRepository.saveAll(Arrays.asList(qna1, qna2));

        // when
        List<QnaEntity> result = qnaRepository.findByDeletedFalseOrderByCreatedAtDesc();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Title 1");
    }

    @Test
    @DisplayName("제목 또는 내용으로 문의사항 검색 테스트")
    void findByDeletedFalseAndTitleContainingOrContentContaining() {
        // given
        QnaEntity qna1 = QnaEntity.builder()
                .title("Important Question")
                .content("Test Content")
                .qnaType("GENERAL")
                .authorId(1)
                .build();

        QnaEntity qna2 = QnaEntity.builder()
                .title("General Question")
                .content("Important Content")
                .qnaType("GENERAL")
                .authorId(1)
                .build();

        ReflectionTestUtils.setField(qna1, "deleted", false);
        ReflectionTestUtils.setField(qna2, "deleted", false);

        qnaRepository.saveAll(Arrays.asList(qna1, qna2));

        // when
        List<QnaEntity> result = qnaRepository.findByDeletedFalseAndTitleContainingOrContentContainingOrderByCreatedAtDesc(
                "Important", "Important");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("title")
                .containsExactlyInAnyOrder("Important Question", "General Question");
    }

    @Test
    @DisplayName("특정 유형의 문의사항 조회 테스트")
    void findByDeletedFalseAndQnaType() {
        // given
        QnaEntity qna1 = QnaEntity.builder()
                .title("General Question 1")
                .content("Content 1")
                .qnaType("GENERAL")
                .authorId(1)
                .build();

        QnaEntity qna2 = QnaEntity.builder()
                .title("Technical Question 1")
                .content("Content 2")
                .qnaType("TECHNICAL")
                .authorId(1)
                .build();

        ReflectionTestUtils.setField(qna1, "deleted", false);
        ReflectionTestUtils.setField(qna2, "deleted", false);

        qnaRepository.saveAll(Arrays.asList(qna1, qna2));

        // when
        List<QnaEntity> result = qnaRepository.findByDeletedFalseAndQnaTypeOrderByCreatedAtDesc("GENERAL");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("General Question 1");
    }
}