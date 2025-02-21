package com.corp.bookiki.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.corp.bookiki.notification.dto.NotificationResponse;
import com.corp.bookiki.notification.entity.NotificationEntity;
import com.corp.bookiki.notification.entity.NotificationStatus;
import com.corp.bookiki.notification.repository.NotificationRepository;
import com.corp.bookiki.notification.service.NotificationService;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.service.UserService;
import com.corp.bookiki.favorite.service.BookFavoriteService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private UserService userService;

	@Mock
	private BookFavoriteService bookFavoriteService;

	@Nested
	@DisplayName("알림 조회 테스트")
	class GetNotifications {

		@Test
		@DisplayName("사용자 알림 목록 조회 성공")
		void getUserNotifications_Success() {
			// given
			UserEntity user = createUser(2);
			NotificationEntity notification = createNotification(user);

			Page<NotificationEntity> notificationPage = new PageImpl<>(
				List.of(notification),
				PageRequest.of(0, 10),
				1
			);

			given(notificationRepository.findActiveNotificationsByUserId(eq(2), any()))
				.willReturn(notificationPage);

			// when
			Page<NotificationResponse> response = notificationService.getUserNotifications(2, PageRequest.of(0, 10));

			// then
			assertThat(response).isNotNull();
			assertThat(response.getContent()).hasSize(1);
			assertThat(response.getContent().get(0).getUserId()).isEqualTo(2);
		}

		@Test
		@DisplayName("알림 상세 조회 성공")
		void getNotification_Success() {
			// given
			UserEntity user = createUser(2);
			NotificationEntity notification = createNotification(user);
			given(notificationRepository.findById(1))
				.willReturn(Optional.of(notification));

			// when
			NotificationResponse response = notificationService.getNotification(1);

			// then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isEqualTo(1);
			assertThat(response.getUserId()).isEqualTo(2);
		}
	}

	@Nested
	@DisplayName("알림 상태 변경 테스트")
	class UpdateNotificationStatus {

		@Test
		@DisplayName("알림 읽음 처리 성공")
		void updateReadStatus_Success() {
			// given
			UserEntity user = createUser(2);
			NotificationEntity notification = createNotification(user);
			given(notificationRepository.findById(1))
				.willReturn(Optional.of(notification));

			// when
			notificationService.UpdateNotificationReadStatus(1);

			// then
			assertThat(notification.getNotificationStatus()).isEqualTo(NotificationStatus.READ);
			verify(notificationRepository).save(notification);
		}

		@Test
		@DisplayName("알림 삭제 처리 성공")
		void updateDeleteStatus_Success() {
			// given
			UserEntity user = createUser(2);
			NotificationEntity notification = createNotification(user);
			given(notificationRepository.findById(1))
				.willReturn(Optional.of(notification));

			// when
			notificationService.UpdateNotificationDeleteStatus(1);

			// then
			assertThat(notification.getNotificationStatus()).isEqualTo(NotificationStatus.DELETE);
			verify(notificationRepository).save(notification);
		}
	}

	private UserEntity createUser(Integer id) {
		UserEntity user = UserEntity.builder()
			.email("test@example.com")
			.userName("테스트 유저")
			.role(Role.USER)
			.build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private NotificationEntity createNotification(UserEntity user) {
		NotificationEntity notification = NotificationEntity.builder()
			.user(user)
			.content("테스트 알림")
			.notificationId(100)
			.notificationType("BOOK_OVERDUE")
			.notificationStatus(NotificationStatus.UNREAD)
			.build();
		ReflectionTestUtils.setField(notification, "id", 1);
		return notification;
	}
}