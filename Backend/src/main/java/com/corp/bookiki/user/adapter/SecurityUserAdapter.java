package com.corp.bookiki.user.adapter;

import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class SecurityUserAdapter implements UserDetails, OAuth2User {
    private final UserEntity user;
    private Map<String, Object> attributes; // OAuth2에서 제공자로부터 받아오는 사용자 정보를 담는 Map

    public SecurityUserAdapter(UserEntity user) {
        this.user = user;
    }

    public SecurityUserAdapter(UserEntity user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getUserName() {
        return user.getUserName();
    }

    public String getCompanyId() {
        return user.getCompanyId();
    }

    public Role getRole() {
        return user.getRole();
    }

    // UserDetails 구현
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
		return true;
	}

    // OAuth2User 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }


}
