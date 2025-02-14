package com.corp.bookiki.notification.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.management.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.notification.entity.NotificationEntity;
import com.corp.bookiki.notification.entity.NotificationStatus;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {

	// 삭제하지 않은 알림 전체 조회
	@Query("SELECT n FROM NotificationEntity n WHERE n.user.id = :userId AND n.notificationStatus != 'DELETE'")
	Page<NotificationEntity> findActiveNotificationsByUserId(@Param("userId") Integer userId, Pageable pageable);

	@Query("SELECT n.createdAt FROM NotificationEntity n WHERE n.notificationType = :notificationType ORDER BY n.createdAt DESC LIMIT 1")
	Optional<LocalDateTime> findLatestCreatedAtByNotificationType(@Param("notificationType") String notificationType);

	boolean existsByUserIdAndNotificationStatus(Integer userId, NotificationStatus notificationStatus);

}
