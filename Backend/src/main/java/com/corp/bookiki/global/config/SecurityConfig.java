package com.corp.bookiki.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.corp.bookiki.auth.service.CustomOAuth2UserService;
import com.corp.bookiki.jwt.filter.JWTFilter;
import com.corp.bookiki.jwt.handler.CustomLogoutHandler;
import com.corp.bookiki.jwt.handler.LoginFailureHandler;
import com.corp.bookiki.jwt.handler.LoginSuccessHandler;
import com.corp.bookiki.jwt.handler.OAuth2AuthenticationFailureHandler;
import com.corp.bookiki.jwt.handler.OAuth2AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

	private final JWTFilter jwtFilter;
	private final LoginFailureHandler loginFailureHandler;
	private final LoginSuccessHandler loginSuccessHandler;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
	private final CustomOAuth2UserService customOAuth2UserService;  // 추가
	private final CustomLogoutHandler logoutHandler;  // 추가

	@Value("$FRONTEND_URL}")
	private String frontendUrl;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("보안 필터 체인 구성 시작");
		http
			.csrf((auth) -> {
				log.debug("보안 필터 체인 구성 시작");
				auth.disable();
			})
			.httpBasic(basic -> basic.disable())
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			//form login
			.formLogin(form -> {
				log.debug("폼 로그인 설정 시작");
				form
					.loginPage("/api/auth/login")      // 로그인 페이지 경로
					.loginProcessingUrl("/api/auth/login-process")  // 실제 로그인 처리 경로
					.usernameParameter("email")     // 이메일을 username으로 사용
					.passwordParameter("password")   // 비밀번호 파라미터명
					.successHandler(loginSuccessHandler)
					.failureHandler(loginFailureHandler)
					.permitAll();
				log.debug("폼 로그인 설정 완료");
			})
			.authorizeHttpRequests(auth -> {
				log.debug("URL 기반 보안 설정 구성");
				auth
					.requestMatchers(
						"/auth/**",  // 인증 관련 엔드포인트
						"/email/**", // 이메일 인증
						"/test-token/**", // 테스트 토큰 발행
						"/oauth2/**",
						"/login/**",
						"/v3/api-docs/**",
						"/v3/api-docs",
						"/swagger-ui/**",
						"/swagger-ui.html",
						"/swagger-resources/**",
						"/webjars/**",
						"/api-docs/**"
					).permitAll()   //  인증 없이 사용
					.requestMatchers("/api/admin").hasRole("ADMIN")  // Role에 따라 권한 부여
					.requestMatchers("/api/user").hasRole("USER")
					.anyRequest().authenticated();   // 그 외 모든 요청은 인증된 사용자만 접근 가능
				log.debug("URL 보안 설정 완료");
			})
			.oauth2Login(oauth2 -> {
				log.debug("OAuth2 로그인 설정 시작");
				oauth2
					.userInfoEndpoint(userInfo -> {
						log.debug("OAuth2 사용자 서비스 설정");
						userInfo.userService(customOAuth2UserService);
					})
					.successHandler(oAuth2AuthenticationSuccessHandler)
					.failureHandler(oAuth2AuthenticationFailureHandler);
				log.debug("OAuth2 로그인 설정 완료");
			})
			.logout(logout -> logout
				.logoutUrl("/api/auth/logout")
				.addLogoutHandler(logoutHandler)
				.logoutSuccessHandler((request, response, authentication) ->
					response.setStatus(HttpServletResponse.SC_OK))
				.permitAll())
			.exceptionHandling(exception -> exception
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					log.warn("접근 거부 처리: {}", accessDeniedException.getMessage());
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				})
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
			.cors(cors -> {
				log.debug("CORS 설정 구성");
				cors
					.configurationSource(request -> {
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
							"X-Requested-With",
							"x-socket-transport"
						));
						configuration.setMaxAge(3600L);
						return configuration;
					});
				log.debug("CORS 설정 완료");
			})
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		log.info("보안 필터 체인 구성 완료");
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Configuration
	public class PasswordConfig {

		@Bean
		public PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}
	}
}
