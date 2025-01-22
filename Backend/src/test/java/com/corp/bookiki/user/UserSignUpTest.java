package com.corp.bookiki.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.corp.bookiki.user.dto.UserSignUpRequest;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.exception.DuplicateCompanyIdException;
import com.corp.bookiki.user.exception.DuplicateEmailException;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserSignUpService;

@ExtendWith(MockitoExtension.class)
class UserSignUpServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserSignUpService userSignUpService;

	private UserSignUpRequest validSignUpRequest;

	@BeforeEach
	void setUp() {
		validSignUpRequest = new UserSignUpRequest();
		validSignUpRequest.setEmail("test@example.com");
		validSignUpRequest.setPassword("password123");
		validSignUpRequest.setUserName("Test User");
		validSignUpRequest.setCompanyId("C12345");
	}

	@Test
	@DisplayName("회원가입 성공 시나리오")
	void testSignUpSuccess() {
		// Given: 유효한 회원가입 요청이 주어지고 이메일과 사번이 중복되지 않은 상황
		when(userRepository.existsByEmail(validSignUpRequest.getEmail())).thenReturn(false);
		when(userRepository.existsByCompanyId(validSignUpRequest.getCompanyId())).thenReturn(false);

		// When: 회원가입을 시도할 때
		userSignUpService.signUp(validSignUpRequest);

		// Then: 사용자 정보가 저장되고 입력한 정보와 일치하는지 확인
		ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
		verify(userRepository).save(userCaptor.capture());

		UserEntity savedUser = userCaptor.getValue();
		assertEquals(validSignUpRequest.getEmail(), savedUser.getEmail());
		assertEquals(validSignUpRequest.getUserName(), savedUser.getUserName());
		assertEquals(validSignUpRequest.getCompanyId(), savedUser.getCompanyId());
	}

	@Test
	@DisplayName("중복 이메일로 회원가입 시 예외 발생")
	void testSignUpWithDuplicateEmail() {
		// Given: 이미 존재하는 이메일로 회원가입을 시도하는 상황
		when(userRepository.existsByEmail(validSignUpRequest.getEmail())).thenReturn(true);

		// When & Then: 회원가입 시도 시 중복 이메일 예외가 발생하고 사용자가 저장되지 않음
		DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
			() -> userSignUpService.signUp(validSignUpRequest));

		assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
		verify(userRepository, never()).save(any(UserEntity.class));
	}

	@Test
	@DisplayName("중복 사번으로 회원가입 시 예외 발생")
	void testSignUpWithDuplicateCompanyId() {
		// Given: 이메일은 유효하지만 이미 존재하는 사번으로 회원가입을 시도하는 상황
		when(userRepository.existsByEmail(validSignUpRequest.getEmail())).thenReturn(false);
		when(userRepository.existsByCompanyId(validSignUpRequest.getCompanyId())).thenReturn(true);

		// When & Then: 회원가입 시도 시 중복 사번 예외가 발생하고 사용자가 저장되지 않음
		DuplicateCompanyIdException exception = assertThrows(DuplicateCompanyIdException.class,
			() -> userSignUpService.signUp(validSignUpRequest));

		assertEquals("이미 사용 중인 사번입니다.", exception.getMessage());
		verify(userRepository, never()).save(any(UserEntity.class));
	}

	@Test
	@DisplayName("이메일 중복 체크 - 중복된 이메일")
	void testCheckEmailDuplicate_DuplicateEmail() {
		// Given: 이미 존재하는 이메일로 중복 체크를 시도하는 상황
		when(userRepository.existsByEmail(anyString())).thenReturn(true);

		// When & Then: 이메일 중복 체크 시 중복 예외가 발생
		DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
			() -> userSignUpService.checkEmailDuplicate("duplicate@example.com"));

		assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
	}

	@Test
	@DisplayName("이메일 중복 체크 - 사용 가능한 이메일")
	void testCheckEmailDuplicate_UniqueEmail() {
		// Given: 존재하지 않는 이메일로 중복 체크를 시도하는 상황
		when(userRepository.existsByEmail(anyString())).thenReturn(false);

		// When & Then: 이메일 중복 체크 시 예외가 발생하지 않음
		assertDoesNotThrow(() -> userSignUpService.checkEmailDuplicate("unique@example.com"));
	}

	@Test
	@DisplayName("사번 중복 체크 - 중복된 사번")
	void testCheckCompanyIdDuplicate_DuplicateCompanyId() {
		// Given: 이미 존재하는 사번으로 중복 체크를 시도하는 상황
		when(userRepository.existsByCompanyId(anyString())).thenReturn(true);

		// When & Then: 사번 중복 체크 시 중복 예외가 발생
		DuplicateCompanyIdException exception = assertThrows(DuplicateCompanyIdException.class,
			() -> userSignUpService.checkEmployeeIdDuplicate("C12345"));

		assertEquals("이미 사용 중인 사번입니다.", exception.getMessage());
	}

	@Test
	@DisplayName("사번 중복 체크 - 사용 가능한 사번")
	void testCheckCompanyIdDuplicate_UniqueCompanyId() {
		// Given: 존재하지 않는 사번으로 중복 체크를 시도하는 상황
		when(userRepository.existsByCompanyId(anyString())).thenReturn(false);

		// When & Then: 사번 중복 체크 시 예외가 발생하지 않음
		assertDoesNotThrow(() -> userSignUpService.checkEmployeeIdDuplicate("C12345"));
	}
}