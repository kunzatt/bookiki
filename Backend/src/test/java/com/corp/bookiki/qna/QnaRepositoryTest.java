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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @DisplayName("페이징을 적용한 문의사항 검색 테스트")
    void findBySearchCriteria() {
        // given
        UserEntity user;
        try {
            user = userRepository.getReferenceById(1);
        } catch (EntityNotFoundException e) {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

        // 테스트용 문의사항 10개 생성
        List<QnaEntity> qnas = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            QnaEntity qna = QnaEntity.builder()
                    .title("Title " + i)
                    .content("Content " + i)
                    .qnaType("GENERAL")
                    .authorId(user.getId())
                    .build();
            ReflectionTestUtils.setField(qna, "deleted", false);
            qnas.add(qna);
        }
        qnaRepository.saveAll(qnas);

        // when
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<QnaEntity> result = qnaRepository.findBySearchCriteria(
                user.getId(),
                "GENERAL",
                "Title",
                pageable
        );

        // then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }
}