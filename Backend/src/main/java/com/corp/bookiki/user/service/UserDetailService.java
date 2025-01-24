package com.corp.bookiki.user.service;

import com.corp.bookiki.user.entity.SecurityUser;
import com.corp.bookiki.user.repository.SecurityUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {

    private final SecurityUserRepository securityUserRepository;

    @Override
    public SecurityUser loadUserByUsername(String email){
        return securityUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }
}
