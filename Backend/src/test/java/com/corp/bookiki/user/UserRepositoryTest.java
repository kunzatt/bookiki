package com.corp.bookiki.user;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
	@Autowired
	private UserRepository userRepository;

	@Test
	void existsByEmail_WhenEmailExists_ThenReturnTrue() {
		// given
		String email = "test@test.com";
		UserEntity user = UserEntity.builder()
			.email(email)
			.password("password123")
			.userName("Test User")
			.companyId("EMP123")
			.build();
		UserEntity savedUser = userRepository.save(user);
		log.info("테스트 사용자 저장 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());

		// when
		boolean exists = userRepository.existsByEmail(email);
		log.info("이메일 중복 확인 결과: email={}, exists={}", email, exists);

		// then
		assertThat(exists).isTrue();
		log.info("이메일 존재 여부 테스트 성공");
	}

	@Test
	void existsByCompanyId_WhenCompanyIdExists_ThenReturnTrue() {
		// given
		String companyId = "EMP123";
		UserEntity user = UserEntity.builder()
			.email("test@test.com")
			.password("password123")
			.userName("Test User")
			.companyId(companyId)
			.build();
		UserEntity savedUser = userRepository.save(user);
		log.info("테스트 사용자 저장 완료: id={}, companyId={}", savedUser.getId(), savedUser.getCompanyId());

		// when
		boolean exists = userRepository.existsByCompanyId(companyId);
		log.info("사원번호 중복 확인 결과: companyId={}, exists={}", companyId, exists);

		// then
		assertThat(exists).isTrue();
		log.info("사원번호 존재 여부 테스트 성공");
	}

	@Test
	@DisplayName("모든 사용자 조회 - 여러 사용자 존재")
	void findAll_WhenUsersExist_ThenReturnList() {
		// given: 여러 사용자 생성 및 저장
		UserEntity user1 = createTestUser("user1@example.com", "CORP001");
		UserEntity user2 = createTestUser("user2@example.com", "CORP002");

		userRepository.save(user1);
		userRepository.save(user2);

		// when: 모든 사용자 조회
		List<UserEntity> users = userRepository.findAll();

		// then: 저장된 사용자 수와 이메일 검증
		assertThat(users)
			.hasSize(2)
			.extracting(UserEntity::getEmail)
			.containsExactly("user1@example.com", "user2@example.com");
	}

	// 테스트 사용자 생성을 위한 헬퍼 메서드
	private UserEntity createTestUser(String email, String companyId) {
		return UserEntity.builder()
			.email(email)
			.password("testPassword")
			.userName("테스트 사용자")
			.companyId(companyId)
			.build();
	}
}