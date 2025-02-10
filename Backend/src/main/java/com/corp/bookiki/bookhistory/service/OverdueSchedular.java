package com.corp.bookiki.bookhistory.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueSchedular {

	private final OverdueService overdueService;

	@Scheduled(cron = "0 0 9 * * *")
	public void checkOverdue() {
		log.info("대출 도서 연체 확인 스케줄러 실행");
		overdueService.processOverdueBooks();
	}
}
