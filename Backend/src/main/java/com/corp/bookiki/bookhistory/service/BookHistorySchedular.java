package com.corp.bookiki.bookhistory.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.corp.bookiki.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookHistorySchedular {

	private final NotificationService notificationService;

	@Scheduled(cron = "0 00 09 * * *")
	public void checkOverdue() {
		log.info("도서 반납일 하루전 알림 생성");
		notificationService.addReturnDeadlineNotification();
	}
}
