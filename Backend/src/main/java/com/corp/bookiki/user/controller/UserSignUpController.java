package com.corp.bookiki.user.controller;

import com.corp.bookiki.user.dto.UserSignUpRequest;
import com.corp.bookiki.user.service.UserSignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/users/email")
@RequiredArgsConstructor
public class UserSignUpController {

    private static final Logger logger = LoggerFactory.getLogger(UserSignUpController.class);
    private final UserSignUpService userSignUpService;

    // 이메일 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> registerWithEmail(@RequestBody UserSignUpRequest request) {
        logger.info("Attempting user signup with email: {}", request.getEmail());
        try {
            // 회원가입 서비스 호출
            userSignUpService.signUp(request);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (Exception e) {
            logger.error("Error during user signup: ", e);
            return ResponseEntity.badRequest().body("회원가입 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 이메일 중복 확인
    @GetMapping("/verify-email")
    public ResponseEntity<String> checkEmailDuplicate(@RequestParam String email) {
        logger.info("Checking email duplicate: {}", email);
        try {
            // 이메일 중복 확인 서비스 호출
            userSignUpService.checkEmailDuplicate(email);
            return ResponseEntity.ok("사용 가능한 이메일입니다.");
        } catch (Exception e) {
            // 오류 발생 시 로깅 및 에러 응답
            logger.error("Error checking email duplicate: ", e);
            return ResponseEntity.badRequest().body("이메일 중복 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 사번 중복 확인
    @GetMapping("/verify-company-id")
    public ResponseEntity<String> checkCompanyId(@RequestParam String companyId) {
        logger.info("Checking company ID duplicate: {}", companyId);
        try {
            // 사번 중복 확인 서비스 호출
            userSignUpService.checkEmployeeIdDuplicate(companyId);
            return ResponseEntity.ok("사용 가능한 사번입니다.");
        } catch (Exception e) {
            // 오류 발생 시 로깅 및 에러 응답
            logger.error("Error checking company ID duplicate: ", e);
            return ResponseEntity.badRequest().body("사번 중복 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}