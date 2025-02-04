package com.corp.bookiki.user.controller;

import com.corp.bookiki.user.dto.TestAccountRequest;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequestMapping("/test-token")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final DataSource dataSource; // 추가

    @PostMapping("/create-test-account")
    public ResponseEntity<String> createTestAccount(@RequestBody TestAccountRequest request) {
        log.info("DB URL: {}", ((HikariDataSource) dataSource).getJdbcUrl());
        log.info("DB Username: {}", ((HikariDataSource) dataSource).getUsername());

        try {
            UserEntity user = UserEntity.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .userName(request.getUserName())
                    .companyId(request.getCompanyId())
                    .role(Role.USER)
                    .provider(Provider.BOOKIKI)
                    .deleted(false)
                    .build();

            UserEntity savedUser = userRepository.save(user);
            log.info("savedUser: {}", savedUser);  // 저장된 사용자 정보 로그

            return ResponseEntity.ok("Test account created - ID: " + savedUser.getId());
        } catch (Exception e) {
            log.error("계정 생성 중 에러 발생", e);
            throw e;
        }
    }
}