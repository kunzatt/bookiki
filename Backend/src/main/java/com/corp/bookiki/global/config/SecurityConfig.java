package com.corp.bookiki.config;

import com.corp.bookiki.jwt.handler.LoginFailureHandler;
import com.corp.bookiki.jwt.handler.LoginSuccessHandler;
import com.corp.bookiki.jwt.handler.OAuth2AuthenticationFailureHandler;
import com.corp.bookiki.jwt.handler.OAuth2AuthenticationSuccessHandler;
import com.corp.bookiki.jwt.filter.JWTFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

	private final JWTFilter jwtFilter;
	private final LoginFailureHandler loginFailureHandler;
	private final LoginSuccessHandler loginSuccessHandler;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

	@Value("${frontend.url}")
	private String frontendUrl;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		log.info("보안 필터 체인 구성 시작");
		http
				.csrf((auth)->{
					log.debug("보안 필터 체인 구성 시작");
					auth.disable();
				})
				//form login
				.formLogin(form -> {
					log.debug("폼 로그인 설정 시작");
					form.loginPage("/api/auth/login")      // 로그인 페이지 경로
							.loginProcessingUrl("/api/auth/login-process")  // 실제 로그인 처리 경로
							.usernameParameter("email")     // 이메일을 username으로 사용
							.passwordParameter("password")   // 비밀번호 파라미터명
							.successHandler(loginSuccessHandler)
							.failureHandler(loginFailureHandler)
							.permitAll();
					log.debug("폼 로그인 설정 완료");
				})
				// http basic 인증 방식 방지
				.httpBasic((auth)->auth.disable())
				.authorizeHttpRequests(auth -> {
					log.debug("URL 기반 보안 설정 구성");
					auth.requestMatchers(
									"/v3/api-docs",
									"/v3/api-docs/**",
									"/swagger-ui/**",
									"/swagger-ui.html",
									"/swagger-resources/**",
									"/webjars/**",
									"/api-docs/**"
							).permitAll()   //  인증 없이 사용
							.requestMatchers("/api/admin").hasRole("ADMIN")  // Role에 따라 권한 부여
							.requestMatchers("/api/user").hasRole("USER")
							.anyRequest()   // 그 외 모든 요청
							.authenticated();   // 인증된 사용자만 접근 가능
					log.debug("URL 보안 설정 완료");
				})
				.oauth2Login(oauth2 -> {
					log.debug("OAuth2 로그인 설정 시작");
					oauth2.userInfoEndpoint(userInfo -> {
								log.debug("OAuth2 사용자 서비스 설정");
								userInfo.userService(customOAuth2UserService);
							})
							.successHandler(oAuth2AuthenticationSuccessHandler)
							.failureHandler(oAuth2AuthenticationFailureHandler)
							.authorizationEndpoint(authorization -> {
								log.debug("OAuth2 인증 기본 URI 설정: /oauth2/authorization");
								authorization.baseUri("/oauth2/authorization");
							});
					log.debug("OAuth2 로그인 설정 완료");
				})
				.addFilterBefore(jwtFilter, LoginFilter.class)
				.sessionManagement(session -> { //세션 사용하지 않음
					log.debug("세션 관리 정책 설정: STATELESS");
					session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
				})
				.exceptionHandling(e -> {
					log.debug("보안 예외 처리 구성");
					e.accessDeniedHandler((request, response, accessDeniedException) -> {
								log.warn("접근 거부 처리: {}", accessDeniedException.getMessage());
								response.setStatus(403);
								//인증된 사용자지만 권한이 부족할 -> 403
							})
							.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
					//인증되지 않은 사용자의 접근 -> 401
				})
				.logout(l -> l  //로그아웃 처리
						.logoutUrl("/api/v1/auth/logout")
						.addLogoutHandler(logoutHandler)
						.logoutSuccessHandler((request, response, authentication) -> {
							response.setStatus(HttpServletResponse.SC_OK);
						})  // 리다이렉트 방지
						.permitAll()
				)
				.cors(corsCustomizer -> {
					log.debug("CORS 설정 구성");
					corsCustomizer.configurationSource(new CorsConfigurationSource() {
						@Override
						public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
							log.debug("CORS 설정 생성 - 요청 URI: {}", request.getRequestURI());

							CorsConfiguration configuration = new CorsConfiguration();
							configuration.setAllowedOriginPatterns(List.of(frontendUrl));
							configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
							configuration.setAllowCredentials(true);
							configuration.addAllowedHeader("*");
							configuration.setExposedHeaders(Arrays.asList(
									"Authorization",
									"Set-Cookie",
									"Access-Control-Allow-Credentials",
									"Access-Control-Allow-Origin"
							));
							configuration.setAllowedHeaders(Arrays.asList(
									"Authorization",
									"Content-Type",
									"Cookie",
									"Access-Control-Allow-Credentials",
									"Access-Control-Allow-Origin",
									"X-Requested-With",  // WebSocket 관련 헤더 추가
									"x-socket-transport"
							));
							configuration.setMaxAge(3600L);

							log.debug("CORS 허용 출처: {}", frontendUrl);
							log.debug("CORS 허용 메서드: {}", configuration.getAllowedMethods());
							log.debug("CORS 노출 헤더: {}", configuration.getExposedHeaders());

							return configuration;
						}
					});
					log.debug("CORS 설정 완료");
				});
		log.info("보안 필터 체인 구성 완료");
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
