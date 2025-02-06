package com.corp.bookiki.global.config;

import com.corp.bookiki.jwt.filter.JwtFilter;
import com.corp.bookiki.user.service.CustomUserDetailsService;
import com.corp.bookiki.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

	private final JwtFilter jwtFilter;
	private final CustomUserDetailsService userDetailsService;
	private final CustomOAuth2UserService customOAuth2UserService;

	@Value("${FRONTEND_URL}")
	private String frontendUrl;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("보안 필터 체인 구성 시작");
		http
			.csrf((auth) -> auth.disable())	// CSRF 보호 비활성화
				.cors(cors -> {
					log.debug("CORS 설정 구성");
					cors
							.configurationSource(request -> {
								CorsConfiguration configuration = new CorsConfiguration();
								configuration.setAllowedOriginPatterns(List.of(frontendUrl,
										"http://i12a206.p.ssafy.io:8088"));

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
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.httpBasic(basic -> basic.disable())	// HTTP Basic 인증 비활성화
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))	// 세션 사용 안함 (JWT 사용)
			.authorizeHttpRequests(auth -> {
				log.debug("URL 기반 보안 설정 구성");
				auth
						.requestMatchers(
								"/api/auth/**",  // 인증 관련 엔드포인트
								"/api/email/**", // 이메일 인증
								"/api/user/signup/**", // 이메일 인증
								"/api/user/login/**", // 이메일 인증
								"/api/books/**",
								"/api/users/books/**",
								"/api/oauth2/**",
								"/api/login/**",
								"/api/api-docs/**",
								"/api/api-docs",
								"/api/swagger-ui/**",
								"/api/swagger-ui.html",
								"/api/swagger-resources/**",
								"/api/webjars/**",
								"/api/test-token/**",
								"/api/configuration/**"
						).permitAll()   //  인증 없이 사용
						.requestMatchers("/api/admin").hasRole("ADMIN")  // Role에 따라 권한 부여
						.anyRequest().authenticated();   // 그 외 모든 요청은 인증된 사용자만 접근 가능
				log.debug("URL 보안 설정 완료");
			})
			.authenticationProvider(authenticationProvider())
			.oauth2Login(oauth2 -> {
				log.debug("OAuth2 로그인 설정 시작");
				oauth2
				.userInfoEndpoint(userInfo -> {
					log.debug("OAuth2 사용자 서비스 설정");
					userInfo.userService(customOAuth2UserService);
				});
			log.debug("OAuth2 로그인 설정 완료");
			})
			.logout(AbstractHttpConfigurer::disable)
			.logout(logout -> {
			logout
					.logoutUrl("/api/auth/logout")
					.permitAll();
			});
//			.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));


		log.info("보안 필터 체인 구성 완료");
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		authProvider.setHideUserNotFoundExceptions(false);  // 예외 처리 명확화
		return authProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
