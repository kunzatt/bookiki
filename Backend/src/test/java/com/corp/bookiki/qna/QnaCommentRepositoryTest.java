package com.corp.bookiki.qna;

import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import com.corp.bookiki.qna.repository.QnaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class QnaCommentRepositoryTest {

    @Autowired
    private QnaCommentRepository qnaCommentRepository;

    @Autowired
    private QnaRepository qnaRepository;

    private QnaEntity savedQna;

    @BeforeEach
    void setUp() {
        // 테스트용 QnA 생성
        QnaEntity qna = QnaEntity.builder()
                .title("Test Question")
                .content("Test Content")
                .qnaType("GENERAL")
                .authorId(1)
                .build();

        ReflectionTestUtils.setField(qna, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(qna, "updatedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(qna, "deleted", false);

        savedQna = qnaRepository.save(qna);
    }

    @Test
    @DisplayName("QnA 댓글 저장 테스트")
    void save() {
        // given
        QnaCommentEntity comment = QnaCommentEntity.builder()
                .qnaId(savedQna.getId())
                .content("Test Comment")
                .authorId(1)
                .build();

        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(comment, "updatedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(comment, "deleted", false);

        // when
        QnaCommentEntity savedComment = qnaCommentRepository.save(comment);

        // then
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("Test Comment");
    }

    @Test
    @DisplayName("QnA ID로 댓글 목록 조회 테스트")
    void findByQnaIdAndDeletedFalseOrderByCreatedAtAsc() {
        // given
        QnaCommentEntity comment1 = QnaCommentEntity.builder()
                .qnaId(savedQna.getId())
                .content("First Comment")
                .authorId(1)
                .build();

        QnaCommentEntity comment2 = QnaCommentEntity.builder()
                .qnaId(savedQna.getId())
                .content("Second Comment")
                .authorId(1)
                .build();

        ReflectionTestUtils.setField(comment1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(comment1, "updatedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(comment1, "deleted", false);

        ReflectionTestUtils.setField(comment2, "createdAt", LocalDateTime.now().plusMinutes(1));
        ReflectionTestUtils.setField(comment2, "updatedAt", LocalDateTime.now().plusMinutes(1));
        ReflectionTestUtils.setField(comment2, "deleted", false);

        qnaCommentRepository.save(comment1);
        qnaCommentRepository.save(comment2);

        // when
        List<QnaCommentEntity> comments = qnaCommentRepository
                .findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(savedQna.getId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getContent()).isEqualTo("First Comment");
        assertThat(comments.get(1).getContent()).isEqualTo("Second Comment");
    }
}