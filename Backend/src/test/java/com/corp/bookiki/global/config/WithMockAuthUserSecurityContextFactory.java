package com.corp.bookiki.global.config;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;

public class WithMockAuthUserSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockAuthUser annotation) {
		AuthUser authUser = AuthUser.builder()
			.id(annotation.id())
			.email(annotation.email())
			.role(Role.valueOf(annotation.role()))
			.build();

		UsernamePasswordAuthenticationToken token =
			new UsernamePasswordAuthenticationToken(
				authUser,
				"password",
				List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role()))
			);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(token);
		return context;
	}
}
