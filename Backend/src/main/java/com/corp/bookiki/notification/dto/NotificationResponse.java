package com.corp.bookiki.notification.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.notification.entity.NotificationEntity;
import com.corp.bookiki.notification.entity.NotificationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "알림")
public class NotificationResponse {
	@Schema(description = "알림 ID", example = "1")
	private Integer id;

	@Schema(description = "요청한 유저 ID", example = "2")
	private Integer userId;

	@Schema(description = "알림 내용", example = "카메라 혹은 통신 오류가 발생하였습니다. 카메라 설치 환경을 확인 해주세요.")
	private String content;

	@Schema(description = "알림 대상의 ID", example = "2")
	private Integer notificationId;

	@Schema(description = "알림 종류", example = "연체 알림")
	private String notificationType;

	@Schema(description = "알림 상태", example = "UNREAD")
	private NotificationStatus notificationStatus;

	@Schema(description = "좋아요 생성 시간")
	private LocalDateTime createdAt;

	public static NotificationResponse from(NotificationEntity entity) {
		return NotificationResponse.builder()
			.id(entity.getId())
			.userId(entity.getUser().getId())
			.content(entity.getContent())
			.notificationId(entity.getNotificationId())
			.notificationType(entity.getNotificationType())
			.notificationStatus(entity.getNotificationStatus())
			.createdAt(entity.getCreatedAt())
			.build();
	}
}