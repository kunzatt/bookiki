package com.corp.bookiki.notification;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.corp.bookiki.notification.entity.NotificationEntity;
import com.corp.bookiki.notification.entity.NotificationStatus;
import com.corp.bookiki.notification.repository.NotificationRepository;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("사용자의 활성 알림 목록 조회 성공")
	void findActiveNotificationsByUserId_Success() {
		// given
		UserEntity user = createAndSaveUser();
		NotificationEntity notification = createAndSaveNotification(user);

		// when
		Page<NotificationEntity> notifications = notificationRepository.findActiveNotificationsByUserId(
			user.getId(),
			PageRequest.of(0, 10)
		);

		// then
		assertThat(notifications).isNotEmpty();
		assertThat(notifications.getContent().get(0).getUser().getId()).isEqualTo(user.getId());
		assertThat(notifications.getContent().get(0).getNotificationStatus()).isNotEqualTo(NotificationStatus.DELETE);
	}

	private UserEntity createAndSaveUser() {
		UserEntity user = UserEntity.builder()
			.email("test@example.com")
			.userName("테스트 유저")
			.password("123456")
			.provider(Provider.BOOKIKI)
			.deleted(false)
			.role(Role.USER)
			.companyId("test222")
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
		return userRepository.save(user);
	}

	private NotificationEntity createAndSaveNotification(UserEntity user) {
		NotificationEntity notification = NotificationEntity.builder()
			.user(user)
			.content("테스트 알림")
			.notificationId(100)
			.notificationType("BOOK_OVERDUE")
			.notificationStatus(NotificationStatus.UNREAD)
			.build();
		return notificationRepository.save(notification);
	}
}