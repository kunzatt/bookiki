package com.corp.bookiki.notification;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(NotificationController.class)
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("알림 컨트롤러 테스트")
@Import({WebMvcConfig.class, CurrentUserArgumentResolver.class, TestSecurityBeansConfig.class, SecurityConfig.class, CookieUtil.class})
class NotificationControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private NotificationService notificationService;

	@Autowired
	private UserRepository userRepository;

	private UserEntity testUserEntity;
	private String testEmail;
	private Authentication auth;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();

		testEmail = "test@example.com";
		testUserEntity = UserEntity.builder()
			.email(testEmail)
			.userName("Test User")
			.role(Role.USER)
			.build();
		ReflectionTestUtils.setField(testUserEntity, "id", 2);

		given(userRepository.findByEmail(eq(testEmail)))
			.willReturn(Optional.of(testUserEntity));

		auth = new UsernamePasswordAuthenticationToken(
			testEmail,
			null,
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);

		log.info("MockMvc 및 테스트 사용자 설정이 완료되었습니다.");
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
					.with(authentication(auth)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").exists())
				.andExpect(jsonPath("$.content[0].userId").value(2))
				.andExpect(jsonPath("$.content[0].content").value("테스트 알림"));
		}

		@Test
		@DisplayName("인증되지 않은 사용자의 조회 시도")
		void getNotifications_Unauthorized() throws Exception {
			mockMvc.perform(get("/api/notifications"))
				.andExpect(status().is3xxRedirection());
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
					.with(authentication(auth)))
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
					.with(authentication(auth)))
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
					.with(authentication(auth)))
				.andExpect(status().isOk());

			verify(notificationService).UpdateNotificationDeleteStatus(eq(1));
		}
	}
}
