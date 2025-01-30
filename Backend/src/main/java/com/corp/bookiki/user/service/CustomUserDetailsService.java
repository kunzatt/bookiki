package com.corp.bookiki.user.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            log.debug("사용자 정보 조회 시작: {}", email);

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음: {}", email);
                        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                    });
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.USER_SEARCH_ERROR);
        }
    }
}