package com.corp.bookiki.global.config;


import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.AuthService;
import com.corp.bookiki.user.service.CustomOAuth2UserService;
import com.corp.bookiki.user.service.CustomUserDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

@TestConfiguration
public class TestSecurityBeansConfig {

	@MockBean
	private UserDetailsService userDetailsService;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private CustomUserDetailsService customUserDetailsService;

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
}
