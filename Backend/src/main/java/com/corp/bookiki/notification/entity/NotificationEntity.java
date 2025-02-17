package com.corp.bookiki.notification.entity;

import java.time.LocalDateTime;

import com.corp.bookiki.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "notifications",
	indexes = {
		@Index(name = "idx_user_id", columnList = "user_id"),
		@Index(name = "idx_notification_id", columnList = "notification_id"),
		@Index(name = "idx_notification_status", columnList = "notification_status"),
		@Index(name = "idx_created_at", columnList = "created_at")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "notification_id")
	private Integer notificationId;

	@Column(nullable = false, length = 100, name = "notification_type")
	private String notificationType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "ENUM('READ', 'UNREAD', 'DELETE') DEFAULT 'UNREAD'", name = "notification_status")
	private NotificationStatus notificationStatus = NotificationStatus.UNREAD;

	@Column(nullable = false, updatable = false, name = "created_at")
	private LocalDateTime createdAt;

	@Builder(toBuilder = true)
	public NotificationEntity(UserEntity user, String content, Integer notificationId,
		String notificationType, NotificationStatus notificationStatus) {
		this.user = user;
		this.content = content;
		this.notificationId = notificationId;
		this.notificationType = notificationType;
		this.notificationStatus = notificationStatus != null ? notificationStatus : NotificationStatus.UNREAD;
		this.createdAt = LocalDateTime.now();
	}

	public void read() {
		this.notificationStatus = NotificationStatus.READ;
	}

	public void delete() {
		this.notificationStatus = NotificationStatus.DELETE;
	}
}