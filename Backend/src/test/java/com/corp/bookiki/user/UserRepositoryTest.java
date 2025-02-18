package com.corp.bookiki.user;

import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
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
			.provider(Provider.BOOKIKI)
			.role(Role.USER)
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
			.provider(Provider.BOOKIKI)
			.role(Role.USER)
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

	// 테스트 사용자 생성을 위한 헬퍼 메서드
	private UserEntity createTestUser(String email, String companyId) {
		return UserEntity.builder()
			.email(email)
			.password("testPassword")
			.userName("테스트 사용자")
			.companyId(companyId)
			.provider(Provider.BOOKIKI)
			.role(Role.USER)
			.build();
	}

	@Test
	void findByEmail_WhenEmailExists_ThenReturnUser() {
		// given
		String email = "test@test.com";
		UserEntity user = createTestUser(email, "EMP123");
		UserEntity savedUser = userRepository.save(user);
		log.info("테스트 사용자 저장 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());

		// when
		Optional<UserEntity> foundUser = userRepository.findByEmail(email);
		log.info("이메일로 사용자 검색 결과: email={}, found={}", email, foundUser.isPresent());

		// then
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getEmail()).isEqualTo(email);
		log.info("이메일로 사용자 검색 테스트 성공");
	}

	@Test
	void findByEmail_WhenEmailDoesNotExist_ThenReturnEmpty() {
		// given
		String nonExistentEmail = "nonexistent@test.com";
		log.info("존재하지 않는 이메일로 검색 시작: email={}", nonExistentEmail);

		// when
		Optional<UserEntity> foundUser = userRepository.findByEmail(nonExistentEmail);
		log.info("이메일로 사용자 검색 결과: email={}, found={}", nonExistentEmail, foundUser.isPresent());

		// then
		assertThat(foundUser).isEmpty();
		log.info("존재하지 않는 이메일 검색 테스트 성공");
	}

	@Test
	void findByEmailAndProvider_WhenBothMatch_ThenReturnUser() {
		// given
		String email = "test@test.com";
		Provider provider = Provider.BOOKIKI;
		UserEntity user = createTestUser(email, "EMP123");
		UserEntity savedUser = userRepository.save(user);
		log.info("테스트 사용자 저장 완료: id={}, email={}, provider={}",
				savedUser.getId(), savedUser.getEmail(), savedUser.getProvider());

		// when
		Optional<UserEntity> foundUser = userRepository.findByEmailAndProvider(email, provider);
		log.info("이메일과 프로바이더로 사용자 검색 결과: email={}, provider={}, found={}",
				email, provider, foundUser.isPresent());

		// then
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getEmail()).isEqualTo(email);
		assertThat(foundUser.get().getProvider()).isEqualTo(provider);
		log.info("이메일과 프로바이더로 사용자 검색 테스트 성공");
	}

	@Test
	void findByEmailAndProvider_WhenProviderDoesNotMatch_ThenReturnEmpty() {
		// given
		String email = "test@test.com";
		UserEntity user = createTestUser(email, "EMP123");
		UserEntity savedUser = userRepository.save(user);
		Provider differentProvider = Provider.GOOGLE;
		log.info("테스트 사용자 저장 완료: id={}, email={}, provider={}",
				savedUser.getId(), savedUser.getEmail(), savedUser.getProvider());

		// when
		Optional<UserEntity> foundUser = userRepository.findByEmailAndProvider(email, differentProvider);
		log.info("다른 프로바이더로 사용자 검색 결과: email={}, provider={}, found={}",
				email, differentProvider, foundUser.isPresent());

		// then
		assertThat(foundUser).isEmpty();
		log.info("잘못된 프로바이더로 검색 테스트 성공");
	}

	@Test
	void findById_WhenIdExists_ThenReturnUser() {
		// given
		UserEntity user = createTestUser("test@test.com", "EMP123");
		UserEntity savedUser = userRepository.save(user);
		log.info("테스트 사용자 저장 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());

		// when
		Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());
		log.info("ID로 사용자 검색 결과: id={}, found={}", savedUser.getId(), foundUser.isPresent());

		// then
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
		log.info("ID로 사용자 검색 테스트 성공");
	}

	@Test
	void findById_WhenIdDoesNotExist_ThenReturnEmpty() {
		// given
		int nonExistentId = 99999;
		log.info("존재하지 않는 ID로 검색 시작: id={}", nonExistentId);

		// when
		Optional<UserEntity> foundUser = userRepository.findById(nonExistentId);
		log.info("ID로 사용자 검색 결과: id={}, found={}", nonExistentId, foundUser.isPresent());

		// then
		assertThat(foundUser).isEmpty();
		log.info("존재하지 않는 ID 검색 테스트 성공");
	}
}