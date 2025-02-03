package com.corp.bookiki.global.config;

import com.corp.bookiki.auth.service.CustomOAuth2UserService;
import com.corp.bookiki.jwt.JwtTokenProvider;
import com.corp.bookiki.jwt.handler.*;
import com.corp.bookiki.jwt.service.JWTService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

@TestConfiguration
public class TestSecurityBeansConfig {

	@MockBean
	private JWTService jwtService;

	@MockBean
	private UserDetailsService userDetailsService;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	@MockBean
	private LoginSuccessHandler loginSuccessHandler;

	@MockBean
	private LoginFailureHandler loginFailureHandler;

	@MockBean
	private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	@MockBean
	private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@MockBean
	private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

	@MockBean
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@MockBean
	private CustomOAuth2UserService customOAuth2UserService;

	@MockBean
	private CustomLogoutHandler customLogoutHandler;

//	@MockBean
//	private CookieUtil cookieUtil;  // 추가
//
//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		http
//				.csrf((csrf) -> csrf.disable())  // 새로운 방식으로 변경
//				.authorizeHttpRequests(authorize -> authorize
//						.requestMatchers("/admin/**").permitAll()
//						.anyRequest().authenticated()
//				)
//				.sessionManagement(session -> session
//						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//				);
//
//		return http.build();
//	}
}
