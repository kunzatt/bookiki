package com.corp.bookiki.qna;

import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
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
class QnaCommentRepositoryTest {

    @Autowired
    private QnaCommentRepository qnaCommentRepository;

    @Test
    void findByQnaIdAndDeletedFalseOrderByCreatedAtAsc() {
        // given
        QnaCommentEntity comment1 = QnaCommentEntity.builder()
                .qnaId(1)
                .content("First Comment")
                .authorId(QnaTestConstants.TEST_ADMIN_ID)
                .build();

        QnaCommentEntity comment2 = QnaCommentEntity.builder()
                .qnaId(1)
                .content("Second Comment")
                .authorId(QnaTestConstants.TEST_USER_ID)
                .build();

        ReflectionTestUtils.setField(comment1, "deleted", false);
        ReflectionTestUtils.setField(comment2, "deleted", true);

        qnaCommentRepository.saveAll(Arrays.asList(comment1, comment2));

        // when
        List<QnaCommentEntity> result = qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(1);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("First Comment");
    }
}