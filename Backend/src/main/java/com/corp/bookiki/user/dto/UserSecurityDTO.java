package com.corp.bookiki.user.dto;

import com.corp.bookiki.auth.entity.AuthProvider;
import com.corp.bookiki.user.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UserSecurityDTO implements UserDetails, OAuth2User {

    private final UserEntity user;
    private Map<String, Object> attributes;
    private AuthProvider provider = AuthProvider.LOCAL;
    private String providerId;
    private boolean emailVerified = false;

    public UserSecurityDTO(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }


}
