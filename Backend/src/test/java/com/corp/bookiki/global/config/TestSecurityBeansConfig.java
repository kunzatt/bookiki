package com.corp.bookiki.global.config;


import com.corp.bookiki.global.security.handler.OAuth2AuthenticationFailureHandler;
import com.corp.bookiki.global.security.handler.OAuth2AuthenticationSuccessHandler;
import com.corp.bookiki.global.security.oauth.CustomOAuth2UserService;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.AuthService;
import com.corp.bookiki.user.service.CustomUserDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.AuthService;
import com.corp.bookiki.user.service.CustomUserDetailsService;

@TestConfiguration
public class TestSecurityBeansConfig {

	@MockBean
	private JwtService jwtService;

	@MockBean
	private CustomUserDetailsService customUserDetailsService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private CustomOAuth2UserService customOAuth2UserService;

	@MockBean
	private AuthService authService;

	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@MockBean
	private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

	@MockBean
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@MockBean
	private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	@MockBean
	private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

}
