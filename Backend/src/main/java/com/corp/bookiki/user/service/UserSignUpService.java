package com.corp.bookiki.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.UserSignUpRequest;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// 주요 기능: 이메일 중복 확인, 사번 중복 확인, 사용자 등록
@Service
@RequiredArgsConstructor
public class UserSignUpService {

	private final UserRepository userRepository;

	// 회원가입 메서드
	@Transactional
	public void signUp(UserSignUpRequest request) {
		// 이메일 중복 확인
		checkEmailDuplicate(request.getEmail());
		// 사번 중복 확인
		checkEmployeeIdDuplicate(request.getCompanyId());

		// UserEntity 빌더를 사용해 새 사용자 객체 생성
		UserEntity user = UserEntity.builder()
			.email(request.getEmail())
			.password(request.getPassword())
			.userName(request.getUserName())
			.companyId(request.getCompanyId())
			.build();

		// 사용자 저장
		userRepository.save(user);
	}

	// 이메일 중복 확인
	public void checkEmailDuplicate(String email) {
		// 이미 존재하는 이메일이면 예외 발생
		if (userRepository.existsByEmail(email)) {
			throw new UserException(ErrorCode.EMAIL_DUPLICATE);
		}
	}

	// 사번 중복 확인
	public void checkEmployeeIdDuplicate(String companyId) {
		// 이미 존재하는 사번이면 예외 발생
		if (userRepository.existsByCompanyId(companyId)) {
			throw new UserException(ErrorCode.COMPANY_ID_DUPLICATE);
		}
	}
}
