package com.corp.bookiki.qna;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaException;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import com.corp.bookiki.qna.repository.QnaRepository;
import com.corp.bookiki.qna.service.QnaService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(MockitoExtension.class)
@Slf4j
class QnaServiceTest {

    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private QnaCommentRepository qnaCommentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QnaService qnaService;

    private UserEntity testUser;
    private UserEntity adminUser;
    private AuthUser testAuthUser;
    private AuthUser adminAuthUser;

    @BeforeEach
    void setUp() {
        Mockito.reset(qnaRepository, qnaCommentRepository, userRepository);

        // 테스트용 일반 사용자 설정
        testUser = UserEntity.builder()
                .id(1)
                .email("test@example.com")
                .userName("Test User")
                .role(Role.USER)
                .build();

        // 테스트용 관리자 사용자 설정
        adminUser = UserEntity.builder()
                .id(2)
                .email("admin@example.com")
                .userName("Admin User")
                .role(Role.ADMIN)
                .build();

        testAuthUser = new AuthUser(1, "test@example.com", Role.USER);
        adminAuthUser = new AuthUser(2, "admin@example.com", Role.ADMIN);

        when(userRepository.getReferenceById(testUser.getId())).thenReturn(testUser);
        when(userRepository.getReferenceById(adminUser.getId())).thenReturn(adminUser);
    }

    @Test
    @DisplayName("문의사항 등록 테스트")
    void createQna() {
        // given
        QnaRequest request = new QnaRequest();
        ReflectionTestUtils.setField(request, "title", "Test Title");
        ReflectionTestUtils.setField(request, "qnaType", "GENERAL");
        ReflectionTestUtils.setField(request, "content", "Test Content");

        ArgumentCaptor<QnaEntity> entityCaptor = ArgumentCaptor.forClass(QnaEntity.class);

        when(qnaRepository.save(any(QnaEntity.class))).thenAnswer(new Answer<QnaEntity>() {
            @Override
            public QnaEntity answer(InvocationOnMock invocation) throws Throwable {
                QnaEntity savedEntity = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedEntity, "id", 1);
                return savedEntity;
            }
        });

        // when
        int qnaId = qnaService.createQna(request, testUser.getId());

        // then
        verify(qnaRepository).save(entityCaptor.capture());
        QnaEntity capturedEntity = entityCaptor.getValue();

        assertThat(qnaId).isEqualTo(1);
        assertThat(capturedEntity.getTitle()).isEqualTo("Test Title");
        assertThat(capturedEntity.getQnaType()).isEqualTo("GENERAL");
        assertThat(capturedEntity.getContent()).isEqualTo("Test Content");
        assertThat(capturedEntity.getAuthorId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("문의사항 목록 조회 테스트 - 일반 사용자")
    void selectQnas_User() {
        // given
        List<QnaEntity> qnas = new ArrayList<>();
        qnas.add(createQnaEntity(1, "Question 1", "GENERAL", false, testUser.getId()));
        qnas.add(createQnaEntity(2, "Question 2", "TECHNICAL", false, testUser.getId()));

        when(qnaRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(qnas);

        // when
        List<QnaEntity> result = qnaService.selectQnas(null, null, null, testAuthUser);

        // then
        assertThat(result).hasSize(2);
        verify(qnaRepository).findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    @DisplayName("문의사항 목록 조회 테스트 - 관리자")
    void selectQnas_Admin() {
        // given
        List<QnaEntity> qnas = new ArrayList<>();
        qnas.add(createQnaEntity(1, "Question 1", "GENERAL", false, testUser.getId()));
        qnas.add(createQnaEntity(2, "Question 2", "TECHNICAL", false, adminUser.getId()));

        when(qnaRepository.findByDeletedFalseOrderByCreatedAtDesc()).thenReturn(qnas);

        // when
        List<QnaEntity> result = qnaService.selectQnas(null, null, null, adminAuthUser);

        // then
        assertThat(result).hasSize(2);
        verify(qnaRepository).findByDeletedFalseOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("문의사항 상세 조회 테스트 - 권한 확인")
    void selectQnaByIdWithComment_Authorization() {
        // given
        int qnaId = 1;
        QnaEntity qna = createQnaEntity(qnaId, "Test Question", "GENERAL", false, testUser.getId());
        List<QnaCommentEntity> comments = new ArrayList<>();
        comments.add(createQnaCommentEntity(1, qnaId, "Test Comment", adminUser.getId()));

        when(qnaRepository.getReferenceById(qnaId)).thenReturn(qna);
        when(qnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(qnaId))
                .thenReturn(comments);
        when(userRepository.getReferenceById(testUser.getId())).thenReturn(testUser);

        // when & then
        // 작성자 본인 조회 가능
        try {
            qnaService.selectQnaByIdWithComment(qnaId, testAuthUser);
        } catch (Exception e) {
            fail("작성자 본인 조회 시 예외가 발생하면 안됩니다.");
        }

        // 관리자 조회 가능
        try {
            qnaService.selectQnaByIdWithComment(qnaId, adminAuthUser);
        } catch (Exception e) {
            fail("관리자 조회 시 예외가 발생하면 안됩니다.");
        }

        // 다른 일반 사용자 조회 불가
        UserEntity otherUser = UserEntity.builder()
                .id(3)
                .email("other@example.com")
                .role(Role.USER)
                .build();
        AuthUser otherAuthUser = new AuthUser(3, "other@example.com", Role.USER);

        try {
            qnaService.selectQnaByIdWithComment(qnaId, otherAuthUser);
            fail("다른 사용자 조회 시 예외가 발생해야 합니다.");
        } catch (QnaException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        }
    }

    @Test
    @DisplayName("문의사항 삭제 테스트 - 권한 확인")
    void deleteQna_Authorization() {
        // given
        int qnaId = 1;
        QnaEntity qna = createQnaEntity(qnaId, "Test Question", "GENERAL", false, testUser.getId());
        when(qnaRepository.getReferenceById(qnaId)).thenReturn(qna);

        // when & then
        // 작성자 본인 삭제 가능
        try {
            qnaService.deleteQna(qnaId, testUser.getId());
        } catch (Exception e) {
            fail("작성자 본인 삭제 시 예외가 발생하면 안됩니다.");
        }

        // 다른 사용자 삭제 불가
        try {
            qnaService.deleteQna(qnaId, adminUser.getId());
            fail("다른 사용자 삭제 시 예외가 발생해야 합니다.");
        } catch (QnaException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        }
    }

    // 테스트용 QnaEntity 생성 헬퍼 메서드
    private QnaEntity createQnaEntity(int id, String title, String qnaType, boolean deleted, int authorId) {
        QnaEntity qna = QnaEntity.builder()
                .title(title)
                .content("Test Content")
                .qnaType(qnaType)
                .authorId(authorId)
                .build();
        ReflectionTestUtils.setField(qna, "id", id);
        ReflectionTestUtils.setField(qna, "deleted", deleted);
        return qna;
    }

    // 테스트용 QnaCommentEntity 생성 헬퍼 메서드
    private QnaCommentEntity createQnaCommentEntity(int id, int qnaId, String content, int authorId) {
        QnaCommentEntity comment = QnaCommentEntity.builder()
                .qnaId(qnaId)
                .content(content)
                .authorId(authorId)
                .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}