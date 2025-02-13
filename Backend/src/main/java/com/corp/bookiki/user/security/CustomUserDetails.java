package com.corp.bookiki.user.security;

import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final UserEntity user;
    private Map<String, Object> attributes;

    // 기존 생성자
    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    // OAuth2User를 위한 생성자 추가
    public CustomUserDetails(UserEntity user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // OAuth2User의 getName() 구현
    @Override
    public String getName() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public boolean needsAdditionalInfo() {
        return user.getCompanyId() == null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !user.getDeleted();
    }

    public Integer getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getCompanyId() {
        return user.getCompanyId();
    }

    public UserEntity getUser() {
        return user;  // user는 클래스의 필드로 저장된 UserEntity
    }

    public Provider getProvider() {
        return user.getProvider();
    }
}