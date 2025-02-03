package com.corp.bookiki.qna;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.repository.QnaRepository;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class QnaRepositoryTest {

    @Autowired
    private QnaRepository qnaRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Test User 생성
        UserEntity testUser = UserEntity.builder()
                .email("test@test.com")
                .password("password")
                .userName("Test User")
                .companyId("CORP001")
                .build();

        // 필수 필드 설정
        ReflectionTestUtils.setField(testUser, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testUser, "updatedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testUser, "deleted", false);
        ReflectionTestUtils.setField(testUser, "role", Role.USER);
        ReflectionTestUtils.setField(testUser, "provider", Provider.BOOKIKI);  // PROVIDER 설정 추가

        userRepository.save(testUser);
    }

    @Test
    @DisplayName("삭제되지 않은 문의사항 조회 테스트")
    void findByDeletedFalseOrderByCreatedAtDesc() {
        // given
        UserEntity user;
        try {
            user = userRepository.getReferenceById(1);
        } catch (EntityNotFoundException e) {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

        QnaEntity qna1 = QnaEntity.builder()
                .title("Title 1")
                .content("Content 1")
                .qnaType("GENERAL")
                .authorId(user.getId())
                .build();

        QnaEntity qna2 = QnaEntity.builder()
                .title("Title 2")
                .content("Content 2")
                .qnaType("GENERAL")
                .authorId(user.getId())
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
    @DisplayName("특정 사용자의 문의사항 조회 테스트")
    void findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc() {
        // given
        UserEntity user;
        try {
            user = userRepository.getReferenceById(1);
        } catch (EntityNotFoundException e) {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

        QnaEntity qna = QnaEntity.builder()
                .title("User Question")
                .content("User Content")
                .qnaType("GENERAL")
                .authorId(user.getId())
                .build();

        qnaRepository.save(qna);

        // when
        List<QnaEntity> result = qnaRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("User Question");
        assertThat(result.get(0).getAuthorId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("문의사항 유형별 조회 테스트")
    void findByDeletedFalseAndQnaTypeOrderByCreatedAtDesc() {
        // given
        UserEntity user;
        try {
            user = userRepository.getReferenceById(1);
        } catch (EntityNotFoundException e) {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

        QnaEntity generalQna = QnaEntity.builder()
                .title("General Question")
                .content("General Content")
                .qnaType("GENERAL")
                .authorId(user.getId())
                .build();

        QnaEntity technicalQna = QnaEntity.builder()
                .title("Technical Question")
                .content("Technical Content")
                .qnaType("TECHNICAL")
                .authorId(user.getId())
                .build();

        qnaRepository.saveAll(Arrays.asList(generalQna, technicalQna));

        // when
        List<QnaEntity> result = qnaRepository.findByDeletedFalseAndQnaTypeOrderByCreatedAtDesc("GENERAL");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQnaType()).isEqualTo("GENERAL");
    }
}
