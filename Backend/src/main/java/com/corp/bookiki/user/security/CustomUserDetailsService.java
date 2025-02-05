package com.corp.bookiki.user.security;

import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    // 이메일로 사용자를 찾음
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getDeleted()) {
            throw new UsernameNotFoundException("User is deleted: " + email);
        }
        /*
            사용자가 없으면 UsernameNotFoundException 발생
            삭제된 사용자인 경우 UsernameNotFoundException 발생
            UserEntity를 CustomUserDetails로 변환하여 반환
         */

        return new CustomUserDetails(user);
    }
}
