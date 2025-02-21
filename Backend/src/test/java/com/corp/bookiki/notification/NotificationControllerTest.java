package com.corp.bookiki.notification;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.notification.controller.NotificationController;
import com.corp.bookiki.notification.dto.NotificationResponse;
import com.corp.bookiki.notification.entity.NotificationStatus;
import com.corp.bookiki.notification.service.NotificationService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
	CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("알림 컨트롤러 테스트")
class NotificationControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private NotificationService notificationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtService jwtService;

	private UserEntity testUserEntity;
	private UserEntity testAdminEntity;
	private String testUserEmail;
	private String testAdminEmail;

	@BeforeEach
	void setup() {
		// 일반 사용자 설정
		testUserEmail = "user@example.com";
		testUserEntity = UserEntity.builder()
			.email(testUserEmail)
			.userName("Test User")
			.role(Role.USER)
			.build();
		ReflectionTestUtils.setField(testUserEntity, "id", 2);

		// 관리자 설정
		testAdminEmail = "admin@example.com";
		testAdminEntity = UserEntity.builder()
			.email(testAdminEmail)
			.userName("Admin User")
			.role(Role.ADMIN)
			.build();
		ReflectionTestUtils.setField(testAdminEntity, "id", 1);

		// 일반 사용자 Claims 설정
		Claims userClaims = mock(Claims.class);
		given(userClaims.getSubject()).willReturn("user:" + testUserEmail);
		given(userClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 3600000));
		given(userClaims.get("authorities", String.class)).willReturn("ROLE_USER");

		// 관리자 Claims 설정
		Claims adminClaims = mock(Claims.class);
		given(adminClaims.getSubject()).willReturn("user:" + testAdminEmail);
		given(adminClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 3600000));
		given(adminClaims.get("authorities", String.class)).willReturn("ROLE_USER,ROLE_ADMIN");

		// JWT Service 모킹
		given(jwtService.validateToken(eq("user-jwt-token"))).willReturn(true);
		given(jwtService.validateToken(eq("admin-jwt-token"))).willReturn(true);
		given(jwtService.extractAllClaims(eq("user-jwt-token"))).willReturn(userClaims);
		given(jwtService.extractAllClaims(eq("admin-jwt-token"))).willReturn(adminClaims);
		given(jwtService.extractEmail(eq("user-jwt-token"))).willReturn(testUserEmail);
		given(jwtService.extractEmail(eq("admin-jwt-token"))).willReturn(testAdminEmail);
		given(jwtService.isTokenExpired(anyString())).willReturn(false);

		// UserRepository 모킹
		given(userRepository.findByEmail(eq(testUserEmail)))
			.willReturn(Optional.of(testUserEntity));
		given(userRepository.findByEmail(eq(testAdminEmail)))
			.willReturn(Optional.of(testAdminEntity));

		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
	}

	@Nested
	@DisplayName("알림 목록 조회 API 테스트")
	class GetNotifications {
		@Test
		@WithMockUser
		@DisplayName("알림 목록 정상 조회")
		void getNotifications_Success() throws Exception {
			// given
			NotificationResponse response = NotificationResponse.builder()
				.id(1)
				.userId(2)
				.content("테스트 알림")
				.notificationId(100)
				.notificationType("BOOK_OVERDUE")
				.notificationStatus(NotificationStatus.UNREAD)
				.createdAt(LocalDateTime.now())
				.build();

			Page<NotificationResponse> page = new PageImpl<>(
				List.of(response),
				PageRequest.of(0, 20),
				1
			);

			given(notificationService.getUserNotifications(eq(2), any()))
				.willReturn(page);

			// when & then
			mockMvc.perform(get("/api/notifications")
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").exists())
				.andExpect(jsonPath("$.content[0].userId").value(2))
				.andExpect(jsonPath("$.content[0].content").value("테스트 알림"));
		}

		@Test
		@DisplayName("인증되지 않은 사용자의 조회 시도")
		void getNotifications_Unauthorized() throws Exception {
			mockMvc.perform(get("/api/notifications"))
				.andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("알림 상세 조회 API 테스트")
	class GetNotification {
		@Test
		@WithMockUser
		@DisplayName("알림 상세 정상 조회")
		void getNotification_Success() throws Exception {
			// given
			NotificationResponse response = NotificationResponse.builder()
				.id(1)
				.userId(2)
				.content("테스트 알림")
				.notificationId(100)
				.notificationType("BOOK_OVERDUE")
				.notificationStatus(NotificationStatus.UNREAD)
				.createdAt(LocalDateTime.now())
				.build();

			given(notificationService.getNotification(eq(1)))
				.willReturn(response);

			// when & then
			mockMvc.perform(get("/api/notifications/1")
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.content").value("테스트 알림"));
		}
	}

	@Nested
	@DisplayName("알림 읽음 처리 API 테스트")
	class UpdateNotificationReadStatus {
		@Test
		@WithMockUser
		@DisplayName("알림 읽음 처리 성공")
		void updateReadStatus_Success() throws Exception {
			mockMvc.perform(put("/api/notifications/1")
					.with(csrf())
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk());

			verify(notificationService).UpdateNotificationReadStatus(eq(1));
		}
	}

	@Nested
	@DisplayName("알림 삭제 API 테스트")
	class DeleteNotification {
		@Test
		@WithMockUser
		@DisplayName("알림 삭제 성공")
		void deleteNotification_Success() throws Exception {
			mockMvc.perform(delete("/api/notifications/1")
					.with(csrf())
					.cookie(getUserJwtCookie()))
				.andExpect(status().isOk());

			verify(notificationService).UpdateNotificationDeleteStatus(eq(1));
		}
	}

	private Cookie getUserJwtCookie() {
		Cookie accessTokenCookie = new Cookie("access_token", "user-jwt-token");
		accessTokenCookie.setPath("/");
		return accessTokenCookie;
	}

	private Cookie getAdminJwtCookie() {
		Cookie accessTokenCookie = new Cookie("access_token", "admin-jwt-token");
		accessTokenCookie.setPath("/");
		return accessTokenCookie;
	}
}
