package com.corp.bookiki.qna;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaCommentException;
import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import com.corp.bookiki.qna.service.QnaCommentService;
import com.corp.bookiki.qna.service.QnaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class QnaCommentServiceTest {

    @Mock
    private QnaCommentRepository qnaCommentRepository;

    @Mock
    private QnaService qnaService;

    @InjectMocks
    private QnaCommentService qnaCommentService;

    private static final int TEST_USER_ID = 1;
    private static final int TEST_QNA_ID = 1;

    @Test
    @DisplayName("댓글 생성 성공 테스트")
    void createQnaComment_Success() {
        // given
        QnaCommentRequest request = new QnaCommentRequest();
        ReflectionTestUtils.setField(request, "qnaId", TEST_QNA_ID);
        ReflectionTestUtils.setField(request, "content", "Test Comment");

        QnaEntity mockQna = QnaEntity.builder()
                .title("Test Question")
                .content("Test Content")
                .qnaType("GENERAL")
                .authorId(TEST_USER_ID)
                .build();
        ReflectionTestUtils.setField(mockQna, "id", TEST_QNA_ID);

        when(qnaService.selectQnaById(TEST_QNA_ID)).thenReturn(mockQna);
        when(qnaCommentRepository.save(any(QnaCommentEntity.class))).thenAnswer(new Answer<QnaCommentEntity>() {
            @Override
            public QnaCommentEntity answer(InvocationOnMock invocation) {
                QnaCommentEntity entity = invocation.getArgument(0);
                ReflectionTestUtils.setField(entity, "id", 1);
                return entity;
            }
        });

        // when
        int commentId = qnaCommentService.createQnaComment(request, TEST_USER_ID);

        // then
        assertThat(commentId).isEqualTo(1);
        verify(qnaCommentRepository).save(any(QnaCommentEntity.class));
    }

    @Test
    @DisplayName("댓글 조회 성공 테스트")
    void selectQnaCommentsByQnaId_Success() {
        // given
        List<QnaCommentEntity> mockComments = Arrays.asList(
                createMockComment(1, "First Comment"),
                createMockComment(2, "Second Comment")
        );

        when(qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(TEST_QNA_ID))
                .thenReturn(mockComments);

        // when
        List<QnaCommentResponse> responses = qnaCommentService.selectQnaCommentsByQnaId(TEST_QNA_ID);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getContent()).isEqualTo("First Comment");
        verify(qnaCommentRepository).findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(TEST_QNA_ID);
    }

    private QnaCommentEntity createMockComment(int id, String content) {
        QnaCommentEntity comment = QnaCommentEntity.builder()
                .qnaId(TEST_QNA_ID)
                .content(content)
                .authorId(TEST_USER_ID)
                .build();
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(comment, "updatedAt", LocalDateTime.now());
        return comment;
    }

    @Test
    @DisplayName("댓글 삭제 성공 테스트")
    void deleteQnaComment_Success() {
        // given
        QnaCommentEntity mockComment = createMockComment(1, "Test Comment");
        when(qnaCommentRepository.getReferenceById(1)).thenReturn(mockComment);

        // when
        qnaCommentService.deleteQnaComment(1);

        // then
        assertThat(mockComment.isDeleted()).isTrue();
        verify(qnaCommentRepository).getReferenceById(1);
    }

    @Test
    @DisplayName("댓글 수정 성공 테스트")
    void updateQnaComment_Success() {
        // given
        QnaCommentEntity mockComment = createMockComment(1, "Original Comment");
        QnaCommentUpdate update = new QnaCommentUpdate();
        ReflectionTestUtils.setField(update, "id", 1);
        ReflectionTestUtils.setField(update, "content", "Updated Comment");

        when(qnaCommentRepository.getReferenceById(1)).thenReturn(mockComment);

        // when
        qnaCommentService.updateQnaComment(update);

        // then
        assertThat(mockComment.getContent()).isEqualTo("Updated Comment");
        verify(qnaCommentRepository).getReferenceById(1);
    }

    @Test
    @DisplayName("댓글 생성 실패 - 빈 내용")
    void createQnaComment_EmptyContent() {
        // given
        QnaCommentRequest request = new QnaCommentRequest();
        ReflectionTestUtils.setField(request, "qnaId", TEST_QNA_ID);
        ReflectionTestUtils.setField(request, "content", "");

        // when & then
        QnaCommentException exception = assertThrows(QnaCommentException.class, () ->
                qnaCommentService.createQnaComment(request, TEST_USER_ID));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        verify(qnaCommentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 빈 내용")
    void updateQnaComment_EmptyContent() {
        // given
        QnaCommentEntity mockComment = createMockComment(1, "Original Comment");
        QnaCommentUpdate update = new QnaCommentUpdate();
        ReflectionTestUtils.setField(update, "id", 1);
        ReflectionTestUtils.setField(update, "content", "");

        when(qnaCommentRepository.getReferenceById(1)).thenReturn(mockComment);

        // when & then
        QnaCommentException exception = assertThrows(QnaCommentException.class, () ->
                qnaCommentService.updateQnaComment(update));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThat(mockComment.getContent()).isEqualTo("Original Comment");
    }

    @Test
    @DisplayName("삭제된 댓글 조회 실패")
    void selectQnaCommentById_Deleted() {
        // given
        QnaCommentEntity mockComment = createMockComment(1, "Test Comment");
        ReflectionTestUtils.setField(mockComment, "deleted", true);
        when(qnaCommentRepository.getReferenceById(1)).thenReturn(mockComment);

        // when & then
        QnaCommentException exception = assertThrows(QnaCommentException.class, () ->
                qnaCommentService.selectQnaCommentById(1));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 조회 실패 - 존재하지 않는 ID")
    void selectQnaCommentById_NotFound() {
        // given
        when(qnaCommentRepository.getReferenceById(anyInt()))
                .thenThrow(EntityNotFoundException.class);

        // when & then
        QnaCommentException exception = assertThrows(QnaCommentException.class, () ->
                qnaCommentService.selectQnaCommentById(1));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 삭제된 댓글 삭제 실패")
    void deleteQnaComment_AlreadyDeleted() {
        // given
        QnaCommentEntity mockComment = createMockComment(1, "Test Comment");
        ReflectionTestUtils.setField(mockComment, "deleted", true);
        when(qnaCommentRepository.getReferenceById(1)).thenReturn(mockComment);

        // when & then
        QnaCommentException exception = assertThrows(QnaCommentException.class, () ->
                qnaCommentService.deleteQnaComment(1));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }
}