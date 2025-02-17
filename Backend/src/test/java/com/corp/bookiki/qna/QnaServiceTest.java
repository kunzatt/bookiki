package com.corp.bookiki.qna;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaException;
import com.corp.bookiki.notification.service.NotificationService;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaCommentRepository;
import com.corp.bookiki.qna.repository.QnaRepository;
import com.corp.bookiki.qna.service.QnaService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.fail;

@Slf4j
@ExtendWith(MockitoExtension.class)
class QnaServiceTest {

    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private QnaCommentRepository qnaCommentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private QnaService qnaService;

    private static final int TEST_USER_ID = 1;
    private static final int TEST_ADMIN_ID = 2;

    @Test
    @DisplayName("문의사항 생성 성공 테스트")
    void createQna_Success() {
        // given
        QnaRequest request = new QnaRequest();
        ReflectionTestUtils.setField(request, "title", "Test Title");
        ReflectionTestUtils.setField(request, "qnaType", "GENERAL");
        ReflectionTestUtils.setField(request, "content", "Test Content");

        when(qnaRepository.save(any(QnaEntity.class))).thenAnswer(new Answer<QnaEntity>() {
            @Override
            public QnaEntity answer(InvocationOnMock invocation) {
                QnaEntity entity = invocation.getArgument(0);
                ReflectionTestUtils.setField(entity, "id", 1);
                return entity;
            }
        });

        // when
        int result = qnaService.createQna(request, TEST_USER_ID);

        // then
        assertThat(result).isEqualTo(1);
        verify(qnaRepository).save(any(QnaEntity.class));
    }

    @Test
    @DisplayName("문의사항 생성 실패 테스트")
    void createQna_Failure() {
        // given
        QnaRequest request = new QnaRequest();
        when(qnaRepository.save(any())).thenThrow(new RuntimeException());

        // when & then
        try {
            qnaService.createQna(request, TEST_USER_ID);
            fail("예외가 발생해야 합니다.");
        } catch (QnaException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    @DisplayName("관리자의 전체 문의사항 조회 테스트")
    void selectQnas_Admin() {
        // given
        // new AuthUser(TEST_ADMIN_ID, "admin@test.com", Role.ADMIN);
        AuthUser authUser = AuthUser
            .builder()
            .id(TEST_ADMIN_ID)
            .email("admin@corp.com")
            .role(Role.ADMIN)
            .build();
        Pageable pageable = PageRequest.of(0, 10);

        UserEntity user = userRepository.findById(TEST_ADMIN_ID).orElse(null);
        List<QnaEntity> mockQnas = Arrays.asList(
                createMockQna(1, "Question 1"),
                createMockQna(2, "Question 2")
        );
        Page<QnaEntity> mockPage = new PageImpl<>(mockQnas, pageable, mockQnas.size());

        when(qnaRepository.findBySearchCriteria(null, null, null, pageable))
                .thenReturn(mockPage);

        // when
        Page<QnaEntity> result = qnaService.selectQnas(null, null, null, authUser, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(qnaRepository).findBySearchCriteria(null, null, null, pageable);
    }

    private QnaEntity createMockQna(Integer id, String title) {
        UserEntity user = UserEntity.builder()
            .email("test@test.com")
            .password("Password")
            .userName("TestUser")
            .companyId("CompanyID")
            .role(Role.USER)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        ReflectionTestUtils.setField(user, "id", 1);

        QnaEntity qna = QnaEntity.builder()
                .title(title)
                .content("Test Content")
                .qnaType("GENERAL")
                .user(user)
                .build();
        ReflectionTestUtils.setField(qna, "id", id);
        return qna;
    }

    private UserEntity createUser(Integer id) {
        UserEntity user = UserEntity.builder()
            .email("test@example.com")
            .userName("테스트 유저")
            .role(Role.USER)
            .createdAt(LocalDateTime.now())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}