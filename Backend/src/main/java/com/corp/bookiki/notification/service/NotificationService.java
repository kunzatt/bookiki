package com.corp.bookiki.notification.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.favorite.service.BookFavoriteService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.NotificationException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.notification.dto.NotificationResponse;
import com.corp.bookiki.notification.entity.NotificationEntity;
import com.corp.bookiki.notification.entity.NotificationInformation;
import com.corp.bookiki.notification.entity.NotificationStatus;
import com.corp.bookiki.notification.repository.NotificationRepository;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 주요 기능: 알림 조회, 알림 생성,
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserService userService;
	private final BookFavoriteService bookFavoriteService;
	private final BookHistoryService bookHistoryService;
	private final BookItemRepository bookItemRepository;

	// 카메라/통신 오류 알림 생성
	@Transactional
	public void addCameraErrorNotification(NotificationInformation notificationInformation) {
		List<UserEntity> admins = userService.getUsersByRole(Role.ADMIN);
		NotificationEntity notificationEntity = createNotificationEntity(notificationInformation, null, null);
		LocalDateTime origin = getTopCreatedAtByType(notificationInformation);
		LocalDateTime now = notificationEntity.getCreatedAt();
		Duration duration = Duration.between(origin, now);
		if(duration.toHours() <= 3) return;
		saveNotificationForUsers(notificationEntity, admins);
	}

	// QR 오류 알림 생성
	@Transactional
	public void addQrScanErrorNotification(NotificationInformation notificationInformation, String title) {
		List<UserEntity> admins = userService.getUsersByRole(Role.ADMIN);
		NotificationEntity notificationEntity = createNotificationEntity(notificationInformation, title, null);
		saveNotificationForUsers(notificationEntity, admins);
	}

	@Transactional
	public void addLostBookNotification(List<Integer> lostBookItemIds) {
		// 1. 한 번에 모든 도서 정보를 조회
		List<BookItemEntity> bookItems = bookItemRepository.findAllById(lostBookItemIds);

		// 2. 알림 엔티티들을 한 번에 생성
		List<NotificationEntity> notifications = bookItems.stream()
			.map(bookItem -> createNotificationEntity(
				NotificationInformation.LOST_BOOK,
				bookItem.getBookInformation().getTitle(),
				bookItem.getId()
			))
			.collect(Collectors.toList());

		// 3. 한 번의 데이터베이스 작업으로 모든 알림을 저장
		notificationRepository.saveAll(notifications);
	}

	// 도서 정리 알림 생성
	@Transactional
	public void addBooKArrangementNotification(NotificationInformation notificationInformation, String title) {
		List<UserEntity> admins = userService.getUsersByRole(Role.ADMIN);
		NotificationEntity notificationEntity = createNotificationEntity(notificationInformation, title, null);
		saveNotificationForUsers(notificationEntity, admins);
	}

	// 도서 연체 알림 생성(관리자한테) - historyId가 담김
	@Transactional
	public void addAdminOverdueNotification(NotificationInformation notificationInformation, String title, Integer notificationId) {
		List<UserEntity> admins = userService.getUsersByRole(Role.ADMIN);
		NotificationEntity notificationEntity = createNotificationEntity(notificationInformation, title, notificationId);
		saveNotificationForUsers(notificationEntity, admins);
	}
	
	// 문의 사항 등록 알림 생성(관리자한테) - qnaId가 담김
	@Transactional
	public void addQnaCreatedNotification(NotificationInformation notificationInformation, String title, Integer notificationId) {
		List<UserEntity> admins = userService.getUsersByRole(Role.ADMIN);
		NotificationEntity notificationEntity = createNotificationEntity(notificationInformation, title, notificationId);
		saveNotificationForUsers(notificationEntity, admins);
	}
	
	// 반납 기한 알림 생성 - bookItemId가 담김
	@Transactional
	public void addReturnDeadlineNotification() {
		List<BookHistoryEntity> bookHistories = bookHistoryService.checkOneDayBeforeReturn();

		bookHistories.forEach(history ->
			saveNotification(
				createNotificationEntity(
					NotificationInformation.RETURN_DEADLINE,
					history.getBookItem().getBookInformation().getTitle(),
					history.getBookItem().getId()
				),
				history.getUser()
			)
		);
	}

	// 연체 알림 생성 - historyId가 담김
	@Transactional
	public void addUserOverDueNotification(NotificationInformation notificationInformation, String title, Integer notificationId, Integer userId) {
		UserEntity user = userService.getUserById(userId);
		NotificationEntity notificationEntity = createNotificationEntity(notificationInformation, title, notificationId);
		saveNotification(notificationEntity, user);
	}

	// 관심 도서 대출 가능 알림 생성 - bookItemId가 담김
	@Transactional
	public void addFavoriteBookAvailableNotification(NotificationInformation notificationInformation, String title, Integer notificationId) {
		List<Integer> userIds = bookFavoriteService.getAllFavoriteUserId(notificationId);
		List<UserEntity> users = userService.getUsersByIds(userIds);
		NotificationEntity notificationEntity = createNotificationEntity(notificationInformation, title, notificationId);
		saveNotificationForUsers(notificationEntity, users);
	}

	// 문의사항 답변 알림 생성 - qnaId가 담김
	@Transactional
	public void addQnaAnsweredNotification(NotificationInformation notificationInformation, String title, Integer notificationId, Integer userId) {
		UserEntity user = userService.getUserById(userId);
		NotificationEntity notificationEntity = createNotificationEntity(notificationInformation, title, notificationId);
		saveNotification(notificationEntity, user);
	}

	// 특정 유저의 알림 조회
	@Transactional(readOnly = true)
	public Page<NotificationResponse> getUserNotifications(Integer userId, Pageable pageable) {
		return notificationRepository.findActiveNotificationsByUserId(userId, pageable)
			.map(NotificationResponse::from);
	}

	// 알림 ID를 이용하여 알림 상세 조회
	@Transactional(readOnly = true)
	public NotificationResponse getNotification(Integer notificationId) {
		NotificationEntity notificationEntity = notificationRepository.findById(notificationId).orElse(null);
		if(notificationEntity == null) throw new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND);
		return NotificationResponse.from(notificationEntity);
	}

	// 알림 ID를 이용하여 알림 읽기 처리
	@Transactional
	public void UpdateNotificationReadStatus(Integer notificationId) {
		NotificationEntity notificationEntity = notificationRepository.findById(notificationId).orElse(null);
		if(notificationEntity == null) throw new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND);
		notificationEntity.read();
		notificationRepository.save(notificationEntity);
	}

	// 알림 ID를 이용하여 알림 삭제 처리
	@Transactional
	public void UpdateNotificationDeleteStatus(Integer notificationId) {
		NotificationEntity notificationEntity = notificationRepository.findById(notificationId).orElse(null);
		if(notificationEntity == null) throw new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND);
		notificationEntity.delete();
		notificationRepository.save(notificationEntity);
	}

	@Transactional(readOnly = true) // 하나라도 읽지 않은 알림이 있는지 판단
	public boolean hasUnreadNotifications(Integer userId) {
		return notificationRepository.existsByUserIdAndNotificationStatus(userId, NotificationStatus.UNREAD);
	}

	@Transactional(readOnly = true)
	protected LocalDateTime getTopCreatedAtByType(NotificationInformation notificationInformation) {
		String notificationType = notificationInformation.getType();
		return notificationRepository.findLatestCreatedAtByNotificationType(notificationType)
			.orElse(LocalDateTime.of(2000, 1, 1, 0, 0,0));
	}

	// 다수의 유저 알림 저장(직접 사용 x)
	@Transactional
	protected void saveNotificationForUsers(NotificationEntity notificationEntity, List<UserEntity> users) {

		List<NotificationEntity> notifications = users.stream()
			.map(user -> notificationEntity.toBuilder()
				.user(user)
				.build())
			.collect(Collectors.toList());

		notificationRepository.saveAll(notifications);
	}

	// 유저 알림 저장 (직접 사용 x)
	@Transactional
	protected void saveNotification(NotificationEntity notification, UserEntity user) {
		NotificationEntity userNotification = notification.toBuilder()
			.user(user)
			.build();
		notificationRepository.save(userNotification);
	}

	private NotificationEntity createNotificationEntity(NotificationInformation info, String title, Integer notificationId) {
		String content = (title != null) ? title + " " + info.getDescription() : info.getDescription();

		return NotificationEntity.builder()
			.content(content)
			.notificationId(notificationId)
			.notificationType(info.getType())
			.notificationStatus(NotificationStatus.UNREAD)
			.build();
	}
}
