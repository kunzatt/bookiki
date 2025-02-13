package com.corp.bookiki.bookhistory.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookReturnSchedular {

	private final BookReturnService bookReturnService;

	@Scheduled(cron = "0 00 09 * * *")
	public void checkMismatchedBooks() {
		log.info("도서 정리 알림 생성");
		bookReturnService.mismatchedBooksAlarms();
	}
}
