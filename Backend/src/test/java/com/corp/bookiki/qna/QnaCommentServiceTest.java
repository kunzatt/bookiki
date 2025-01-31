package com.corp.bookiki.qna;

import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import com.corp.bookiki.qna.service.QnaCommentService;
import com.corp.bookiki.qna.service.QnaService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class QnaCommentServiceTest {

    @Mock
    private QnaCommentRepository qnaCommentRepository;

    @Mock
    private QnaService qnaService;

    @InjectMocks
    private QnaCommentService qnaCommentService;

    @BeforeEach
    void setUp() {
        Mockito.reset(qnaCommentRepository, qnaService);
    }

    @Test
    @DisplayName("댓글 등록 테스트")
    void createComment() {
        // given
        QnaCommentRequest request = new QnaCommentRequest();
        ReflectionTestUtils.setField(request, "qnaId", 1);
        ReflectionTestUtils.setField(request, "content", "Test Comment");

        QnaCommentEntity entity = request.toEntity(QnaTestConstants.TEST_ADMIN_ID);
        ReflectionTestUtils.setField(entity, "id", 1);

        when(qnaService.selectQnaById(1)).thenReturn(mock(QnaEntity.class));
        when(qnaCommentRepository.save(any(QnaCommentEntity.class))).thenReturn(entity);

        // when
        int commentId = qnaCommentService.createQnaComment(request, QnaTestConstants.TEST_ADMIN_ID);

        // then
        verify(qnaCommentRepository, times(1)).save(any(QnaCommentEntity.class));
        assertThat(commentId).isEqualTo(entity.getId());
    }

    @Test
    @DisplayName("QnA별 댓글 목록 조회 테스트")
    void selectQnaCommentsByQnaId() {
        // given
        int qnaId = 1;
        List<QnaCommentEntity> comments = Arrays.asList(
                createCommentEntity(1, qnaId, "Comment 1"),
                createCommentEntity(2, qnaId, "Comment 2")
        );

        when(qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(qnaId))
                .thenReturn(comments);

        // when
        List<QnaCommentResponse> result = qnaCommentService.selectQnaCommentsByQnaId(qnaId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Comment 1");
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteComment() {
        // given
        int commentId = 1;
        QnaCommentEntity comment = createCommentEntity(commentId, 1, "Test Comment");
        when(qnaCommentRepository.getReferenceById(commentId)).thenReturn(comment);

        // when
        qnaCommentService.deleteQnaComment(commentId);

        // then
        assertThat(comment.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void updateComment() {
        // given
        int commentId = 1;
        QnaCommentEntity comment = createCommentEntity(commentId, 1, "Old Content");

        QnaCommentUpdate update = new QnaCommentUpdate();
        ReflectionTestUtils.setField(update, "id", commentId);
        ReflectionTestUtils.setField(update, "content", "New Content");

        when(qnaCommentRepository.getReferenceById(commentId)).thenReturn(comment);

        // when
        qnaCommentService.updateQnaComment(update);

        // then
        assertThat(comment.getContent()).isEqualTo("New Content");
    }

    private QnaCommentEntity createCommentEntity(int id, int qnaId, String content) {
        QnaCommentEntity comment = QnaCommentEntity.builder()
                .qnaId(qnaId)
                .content(content)
                .authorId(QnaTestConstants.TEST_ADMIN_ID)
                .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}