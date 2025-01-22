package com.corp.bookiki.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.user.dto.UserSignUpRequest;
import com.corp.bookiki.user.service.UserSignUpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user/signup")
@RequiredArgsConstructor
@Slf4j
public class UserSignUpController {

	private final UserSignUpService userSignUpService;

	// 이메일 회원가입
	@PostMapping
	public ResponseEntity<String> registerWithEmail(@RequestBody UserSignUpRequest request) {
		log.info("이메일로 회원가입: {}", request.getEmail());
		userSignUpService.signUp(request);
		return ResponseEntity.ok("회원가입이 완료되었습니다.");
	}

	// 이메일 중복 확인
	@GetMapping("/email/check")
	public ResponseEntity<String> checkEmailDuplicate(@RequestParam String email) {
		userSignUpService.checkEmailDuplicate(email);
		log.info("이메일 중복 확인: {}", email);
		return ResponseEntity.ok("이메일이 중복 되지 않습니다.");
	}

	// 사번 중복 확인
	@GetMapping("/company-id/check")
	public ResponseEntity<String> checkCompanyId(@RequestParam String companyId) {
		log.info("사번 중복 확인: {}", companyId);
		userSignUpService.checkEmployeeIdDuplicate(companyId);
		return ResponseEntity.ok("사번이 중복 되지 않습니다.");
	}
}
