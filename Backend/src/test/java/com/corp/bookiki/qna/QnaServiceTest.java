package com.corp.bookiki.qna;

import com.corp.bookiki.global.error.exception.QnaException;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaDetailResponse;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.dto.QnaUpdate;
import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import com.corp.bookiki.qna.repository.QnaRepository;
import com.corp.bookiki.qna.service.QnaCommentService;
import com.corp.bookiki.qna.service.QnaService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class QnaServiceTest {

    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private QnaCommentRepository qnaCommentRepository;

    @Mock
    private QnaCommentService qnaCommentService;

    @InjectMocks
    private QnaService qnaService;

    @BeforeEach
    void setUp() {
        Mockito.reset(qnaRepository, qnaCommentRepository, qnaCommentService);
    }

    @Test
    @DisplayName("문의사항 등록 테스트")
    void createQna() {
        // given
        QnaRequest request = new QnaRequest();
        ReflectionTestUtils.setField(request, "title", "Test Title");
        ReflectionTestUtils.setField(request, "qnaType", "GENERAL");
        ReflectionTestUtils.setField(request, "content", "Test Content");

        // Capture the entity that will be saved
        ArgumentCaptor<QnaEntity> entityCaptor = ArgumentCaptor.forClass(QnaEntity.class);

        // Setup the mock to return the captured entity with ID set
        when(qnaRepository.save(any(QnaEntity.class))).thenAnswer(invocation -> {
            QnaEntity savedEntity = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedEntity, "id", 1);
            return savedEntity;
        });

        // when
        int qnaId = qnaService.createQna(request, 1);

        // then
        verify(qnaRepository).save(entityCaptor.capture());
        QnaEntity capturedEntity = entityCaptor.getValue();

        assertThat(qnaId).isEqualTo(1);
        assertThat(capturedEntity.getTitle()).isEqualTo("Test Title");
        assertThat(capturedEntity.getQnaType()).isEqualTo("GENERAL");
        assertThat(capturedEntity.getContent()).isEqualTo("Test Content");
    }

    @Test
    @DisplayName("문의사항 목록 조회 테스트 - 기본")
    void selectQnas_Basic() {
        // given
        List<QnaEntity> qnas = Arrays.asList(
                createQnaEntity(1, "Question 1", "GENERAL", false),
                createQnaEntity(2, "Question 2", "TECHNICAL", false)
        );

        when(qnaRepository.findByDeletedFalseOrderByCreatedAtDesc()).thenReturn(qnas);

        // when
        List<QnaEntity> result = qnaService.selectQnas(null, null, null);

        // then
        assertThat(result).hasSize(2);
        verify(qnaRepository, times(1)).findByDeletedFalseOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("문의사항 목록 조회 테스트 - 답변 여부 필터링")
    void selectQnas_WithAnsweredFilter() {
        // given
        QnaEntity qna1 = createQnaEntity(1, "Question 1", "GENERAL", false);
        QnaEntity qna2 = createQnaEntity(2, "Question 2", "TECHNICAL", false);
        List<QnaEntity> qnas = Arrays.asList(qna1, qna2);

        when(qnaRepository.findByDeletedFalseOrderByCreatedAtDesc()).thenReturn(qnas);

        QnaCommentEntity comment = QnaCommentEntity.builder()
                .qnaId(1)
                .content("Test Comment")
                .authorId(1)
                .build();
        when(qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(1))
                .thenReturn(Collections.singletonList(comment));
        when(qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(2))
                .thenReturn(Collections.emptyList());

        // when
        List<QnaEntity> result = qnaService.selectQnas(null, null, true);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("문의사항 목록 조회 테스트 - 타입 필터링")
    void selectQnas_WithTypeFilter() {
        // given
        List<QnaEntity> qnas = Arrays.asList(
                createQnaEntity(1, "Question 1", "GENERAL", false),
                createQnaEntity(2, "Question 2", "TECHNICAL", false)
        );

        when(qnaRepository.findByDeletedFalseOrderByCreatedAtDesc()).thenReturn(qnas);

        // when
        List<QnaEntity> result = qnaService.selectQnas(null, "GENERAL", null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQnaType()).isEqualTo("GENERAL");
    }

    @Test
    @DisplayName("문의사항 목록 조회 테스트 - 키워드 검색")
    void selectQnas_WithKeywordSearch() {
        // given
        List<QnaEntity> qnas = Arrays.asList(
                createQnaEntity(1, "Important Question", "GENERAL", false),
                createQnaEntity(2, "Normal Question", "TECHNICAL", false)
        );

        when(qnaRepository.findByDeletedFalseOrderByCreatedAtDesc()).thenReturn(qnas);

        // when
        List<QnaEntity> result = qnaService.selectQnas("Important", null, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Important");
    }

    @Test
    @DisplayName("문의사항 상세 조회 테스트")
    void selectQnaById() {
        // given
        int qnaId = 1;
        QnaEntity qna = createQnaEntity(qnaId, "Test Question", "GENERAL", false);

        when(qnaRepository.getReferenceById(qnaId)).thenReturn(qna);

        // when
        QnaEntity result = qnaService.selectQnaById(qnaId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(qnaId);
    }

    @Test
    @DisplayName("존재하지 않는 문의사항 조회 시 예외 발생")
    void selectQnaById_NotFound() {
        // given
        int qnaId = 999;
        when(qnaRepository.getReferenceById(qnaId)).thenReturn(null);

        // when & then
        assertThrows(QnaException.class, () -> qnaService.selectQnaById(qnaId));
    }

    @Test
    @DisplayName("문의사항 상세 조회 테스트 - 댓글 포함")
    void selectQnaByIdWithComment() {
        // given
        int qnaId = 1;
        QnaEntity qna = createQnaEntity(qnaId, "Test Question", "GENERAL", false);
        List<QnaCommentResponse> comments = Collections.singletonList(
                new QnaCommentResponse(createQnaCommentEntity(1, qnaId, "Test Comment"))
        );

        when(qnaRepository.getReferenceById(qnaId)).thenReturn(qna);
        when(qnaCommentService.selectQnaCommentsByQnaId(qnaId)).thenReturn(comments);

        // when
        QnaDetailResponse result = qnaService.selectQnaByIdWithComment(qnaId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAuthorName()).isEqualTo(QnaTestConstants.TEST_USER_NAME);
        assertThat(result.getComments()).hasSize(1);
    }


    @Test
    @DisplayName("문의사항 삭제 테스트")
    void deleteQna() {
        // given
        int qnaId = 1;
        QnaEntity qna = createQnaEntity(qnaId, "Test Question", "GENERAL", false);
        when(qnaRepository.getReferenceById(qnaId)).thenReturn(qna);

        // when
        qnaService.deleteQna(qnaId);

        // then
        assertThat(qna.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("문의사항 수정 테스트")
    void updateQna() {
        // given
        int qnaId = 1;
        QnaEntity qna = createQnaEntity(qnaId, "Old Title", "GENERAL", false);

        QnaUpdate update = new QnaUpdate();
        ReflectionTestUtils.setField(update, "id", qnaId);
        ReflectionTestUtils.setField(update, "title", "New Title");
        ReflectionTestUtils.setField(update, "qnaType", "TECHNICAL");
        ReflectionTestUtils.setField(update, "content", "New Content");

        when(qnaRepository.getReferenceById(qnaId)).thenReturn(qna);

        // when
        qnaService.updateQna(update);

        // then
        assertThat(qna.getTitle()).isEqualTo("New Title");
        assertThat(qna.getQnaType()).isEqualTo("TECHNICAL");
        assertThat(qna.getContent()).isEqualTo("New Content");
    }

    // 테스트용 QnaEntity 생성 헬퍼 메서드
    private QnaEntity createQnaEntity(int id, String title, String qnaType, boolean deleted) {
        QnaEntity qna = QnaEntity.builder()
                .title(title)
                .content("Test Content")
                .qnaType(qnaType)
                .authorId(QnaTestConstants.TEST_USER_ID)
                .build();
        ReflectionTestUtils.setField(qna, "id", id);
        ReflectionTestUtils.setField(qna, "deleted", deleted);
        return qna;
    }

    // 테스트용 QnaCommentEntity 생성 헬퍼 메서드
    private QnaCommentEntity createQnaCommentEntity(int id, int qnaId, String content) {
        QnaCommentEntity comment = QnaCommentEntity.builder()
                .qnaId(qnaId)
                .content(content)
                .authorId(QnaTestConstants.TEST_ADMIN_ID)
                .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}