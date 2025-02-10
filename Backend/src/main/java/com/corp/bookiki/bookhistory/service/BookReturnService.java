package com.corp.bookiki.bookhistory.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.dto.BookReturnRequest;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookReturnService {

	private final BookItemRepository bookItemRepository;
	private final BookHistoryRepository bookHistoryRepository;

	private List<String> previousOcrResults = new ArrayList<>();

	public void processScanResults(BookReturnRequest bookReturnRequest) {
		// 1. QR로 읽은 책들 자동 반납 처리
		List<BookItemEntity> returnedBooks = processQrResults(bookReturnRequest.getScannedBookItemIds());

		// 2. OCR 결과 분석
		List<String> newOcrTexts = analyzeOcrChanges(bookReturnRequest.getOcrResults());

		// 3. 새로운 OCR 결과가 있다면 관리자에게 알림
		if (!newOcrTexts.isEmpty()) {
			// TODO: 관리자 알림 구현 필요
		}
	}

	private List<BookItemEntity> processQrResults(List<Integer> scannedIds) {
		List<BookItemEntity> borrowedBooks = bookItemRepository.findAllById(scannedIds)
			.stream()
			.filter(book -> book.getBookStatus() == BookStatus.BORROWED && !book.getDeleted())
			.collect(Collectors.toList());

		for (BookItemEntity book : borrowedBooks) {
			processReturn(book);
		}

		return borrowedBooks;
	}

	private List<String> analyzeOcrChanges(List<String> currentOcrResults) {
		List<String> newOcrTexts = new ArrayList<>();

		if (currentOcrResults.size() > previousOcrResults.size()) {
			newOcrTexts = findNewOcrTexts(currentOcrResults);
		}

		this.previousOcrResults = new ArrayList<>(currentOcrResults);
		return newOcrTexts;
	}

	private List<String> findNewOcrTexts(List<String> currentOcrResults) {
		return currentOcrResults.stream()
			.filter(current -> !previousOcrResults.contains(current))
			.collect(Collectors.toList());
	}

	private void processReturn(BookItemEntity bookItem) {
		bookItem.returnBook();

		BookHistoryEntity latestHistory = bookItem.getBookHistories().stream()
			.filter(history -> history.getReturnedAt() == null)
			.max(Comparator.comparing(BookHistoryEntity::getBorrowedAt))
			.orElseThrow(() -> new BookItemException(ErrorCode.INVALID_BOOK_STATUS));

		latestHistory.returnBook();

	}
}