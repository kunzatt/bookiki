package com.corp.bookiki.user;

import static org.assertj.core.api.Assertions.*;

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
}