package com.corp.bookiki.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.favorite.dto.BookFavoriteResponse;
import com.corp.bookiki.favorite.service.BookFavoriteService;
import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.notification.dto.NotificationResponse;
import com.corp.bookiki.notification.service.NotificationService;
import com.corp.bookiki.user.dto.AuthUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "알림 관련 API")
@Slf4j
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("")
	@Operation(summary = "사용자 알림 목록 조회", description = "로그인한 사용자의 알림 목록을 페이지네이션하여 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "알림 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = NotificationResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
		@CurrentUser AuthUser authUser,
		Pageable pageable) {

		log.info("사용자 알림 목록 조회: userId={}", authUser.getId());
		return ResponseEntity.ok(notificationService.getUserNotifications(authUser.getId(), pageable));
	}

	@GetMapping("/{notificationId}")
	@Operation(summary = "알림 상세 조회", description = "특정 알림의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "알림 조회 성공"
		),
		@ApiResponse(
			responseCode = "404",
			description = "알림을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	public ResponseEntity<NotificationResponse> getNotification(
		@Parameter(description = "조회할 알림 ID", required = true, example = "1")
		@PathVariable Integer notificationId) {

		log.info("알림 상세 조회: notificationId={}", notificationId);
		return ResponseEntity.ok(notificationService.getNotification(notificationId));
	}

	@PutMapping("/{notificationId}")
	@Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "알림 읽음 처리 성공"
		),
		@ApiResponse(
			responseCode = "404",
			description = "알림을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	public ResponseEntity<Void> updateNotificationReadStatus(
		@Parameter(description = "읽음 처리할 알림 ID", required = true, example = "1")
		@PathVariable Integer notificationId) {

		log.info("알림 읽음 처리: notificationId={}", notificationId);
		notificationService.UpdateNotificationReadStatus(notificationId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{notificationId}")
	@Operation(summary = "알림 삭제", description = "특정 알림을 삭제 처리합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "알림 삭제 성공"
		),
		@ApiResponse(
			responseCode = "404",
			description = "알림을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	public ResponseEntity<Void> deleteNotification(
		@Parameter(description = "삭제할 알림 ID", required = true, example = "1")
		@PathVariable Integer notificationId) {

		log.info("알림 삭제: notificationId={}", notificationId);
		notificationService.UpdateNotificationDeleteStatus(notificationId);
		return ResponseEntity.ok().build();
	}
}