package com.corp.bookiki.bookhistory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookhistory.dto.BookReturnRequest;
import com.corp.bookiki.bookhistory.service.BookReturnService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/books/return")
@RequiredArgsConstructor
public class BookReturnController {

	private final BookReturnService bookReturnService;

	@PostMapping("/scan")
	public ResponseEntity<Void> processScanResult(
		@RequestBody BookReturnRequest request
	) {
		log.info("QR count: {}, OCR count: {}",
			request.getScannedBookItemIds().size(),
			request.getOcrResults().size()
		);

		bookReturnService.processScanResults(request);

		return ResponseEntity.ok().build();
	}
}