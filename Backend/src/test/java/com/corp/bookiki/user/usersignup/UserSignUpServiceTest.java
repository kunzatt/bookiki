package com.corp.bookiki.user.usersignup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.UserSignUpRequest;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserSignUpService;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
class UserSignUpServiceTest {
	@InjectMocks
	private UserSignUpService userSignUpService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserRepository userRepository;

	@Nested
	class SignUp {
		@Test
		void signUp_WhenRequestValid_ThenSuccess() {
			// given
			UserSignUpRequest request = createSignUpRequest("test@test.com", "EMP123");
			log.info("회원가입 요청 생성: email={}, companyId={}", request.getEmail(), request.getCompanyId());

			given(userRepository.existsByEmail(anyString())).willReturn(false);
			given(userRepository.existsByCompanyId(anyString())).willReturn(false);
			log.info("Mock 설정 완료: 이메일/사원번호 중복 없음");

			// when & then
			assertDoesNotThrow(() -> {
				userSignUpService.signUp(request);
				log.info("회원가입 성공");
			});
			verify(userRepository).save(any(UserEntity.class));
			log.info("회원정보 저장 검증 완료");
		}

		@Test
		@DisplayName("이미 존재하는 이메일이면 회원가입에 실패한다")
		void signUp_WhenEmailExists_ThenFail() {
			// given
			UserSignUpRequest request = createSignUpRequest("test@test.com", "EMP123");
			log.info("회원가입 요청 생성: email={}", request.getEmail());

			given(userRepository.existsByEmail(anyString())).willReturn(true);
			log.info("Mock 설정 완료: 이메일 중복 있음");

			// when & then
			UserException exception = assertThrows(UserException.class, () -> userSignUpService.signUp(request));
			log.error("예상된 실패 발생: {}", exception.getMessage());

			verify(userRepository, never()).save(any(UserEntity.class));
			log.info("회원정보 저장 시도 없음 검증 완료");
		}

		@Test
		@DisplayName("이미 존재하는 사원번호면 회원가입에 실패한다")
		void signUp_WhenCompanyIdExists_ThenFail() {
			// given
			UserSignUpRequest request = createSignUpRequest("test@test.com", "EMP123");
			log.info("회원가입 요청 생성: email={}, companyId={}", request.getEmail(), request.getCompanyId());

			given(userRepository.existsByEmail(anyString())).willReturn(false);
			given(userRepository.existsByCompanyId(anyString())).willReturn(true);
			log.info("Mock 설정 완료: 사원번호 중복 있음");

			// when & then
			UserException exception = assertThrows(UserException.class, () -> userSignUpService.signUp(request));
			log.error("예상된 실패 발생: {}", exception.getMessage());

			verify(userRepository, never()).save(any(UserEntity.class));
			log.info("회원정보 저장 시도 없음 검증 완료");
		}
	}

	@Nested
	@DisplayName("이메일 중복 확인 테스트")
	class CheckEmail {
		@Test
		void checkEmail_WhenNotExists_ThenSuccess() {
			// given
			String email = "test@test.com";
			log.info("이메일 중복 확인 요청: {}", email);

			given(userRepository.existsByEmail(email)).willReturn(false);
			log.info("Mock 설정 완료: 이메일 중복 없음");

			// when & then
			assertDoesNotThrow(() -> {
				userSignUpService.checkEmailDuplicate(email);
				log.info("이메일 중복 확인 성공");
			});
		}

		@Test
		void checkEmail_WhenExists_ThenFail() {
			// given
			String email = "test@test.com";
			log.info("이메일 중복 확인 요청: {}", email);

			given(userRepository.existsByEmail(email)).willReturn(true);
			log.info("Mock 설정 완료: 이메일 중복 있음");

			// when & then
			UserException exception = assertThrows(UserException.class,
				() -> userSignUpService.checkEmailDuplicate(email));
			log.error("예상된 실패 발생: {}", exception.getMessage());
		}
	}

	@Nested
	class CheckCompanyId {
		@Test
		void checkCompanyId_WhenNotExists_ThenSuccess() {
			// given
			String companyId = "EMP123";
			log.info("사원번호 중복 확인 요청: {}", companyId);

			given(userRepository.existsByCompanyId(companyId)).willReturn(false);
			log.info("Mock 설정 완료: 사원번호 중복 없음");

			// when & then
			assertDoesNotThrow(() -> {
				userSignUpService.checkEmployeeIdDuplicate(companyId);
				log.info("사원번호 중복 확인 성공");
			});
		}

		@Test
		void checkCompanyId_WhenExists_ThenFail() {
			// given
			String companyId = "EMP123";
			log.info("사원번호 중복 확인 요청: {}", companyId);

			given(userRepository.existsByCompanyId(companyId)).willReturn(true);
			log.info("Mock 설정 완료: 사원번호 중복 있음");

			// when & then
			UserException exception = assertThrows(UserException.class,
				() -> userSignUpService.checkEmployeeIdDuplicate(companyId));
			log.error("예상된 실패 발생: {}", exception.getMessage());
		}
	}

	private UserSignUpRequest createSignUpRequest(String email, String companyId) {
		UserSignUpRequest request = new UserSignUpRequest();
		request.setEmail(email);
		request.setPassword("password123");
		request.setUserName("Test User");
		request.setCompanyId(companyId);
		log.debug("회원가입 요청 객체 생성: {}", request);
		return request;
	}
}