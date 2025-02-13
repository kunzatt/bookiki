package com.corp.bookiki.bookhistory.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.dto.BookReturnRequest;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.global.error.exception.BookItemException;
import com.corp.bookiki.global.error.exception.ShelfException;
import com.corp.bookiki.notice.service.NoticeService;
import com.corp.bookiki.notification.entity.NotificationInformation;
import com.corp.bookiki.notification.service.NotificationService;
import com.corp.bookiki.shelf.entity.ShelfEntity;
import com.corp.bookiki.shelf.repository.ShelfRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookReturnService {

	private final BookItemRepository bookItemRepository;
	private final BookHistoryRepository bookHistoryRepository;
	private final ShelfRepository shelfRepository;
	private final NotificationService notificationService;
	private final NoticeService noticeService;
	private static List<BookItemEntity> mismatchedBooks;
	private static Map<Integer, List<Integer>> shelfBookItemsMap;

	// OCR 결과 유사성 분석을 위한 내부 클래스
	private static class OcrSimilarityAnalyzer {
		/**
		 * Levenshtein Distance 알고리즘 구현
		 * @param s1 첫 번째 문자열
		 * @param s2 두 번째 문자열
		 * @return 두 문자열 간 편집 거리
		 */
		public static int calculateLevenshteinDistance(String s1, String s2) {
			// null 체크
			if (s1 == null) return s2 == null ? 0 : s2.length();
			if (s2 == null) return s1.length();

			// 2차원 DP 테이블 초기화
			int[][] dp = new int[s1.length() + 1][s2.length() + 1];

			// 초기 값 설정
			for (int i = 0; i <= s1.length(); i++) {
				dp[i][0] = i;
			}
			for (int j = 0; j <= s2.length(); j++) {
				dp[0][j] = j;
			}

			// DP 테이블 채우기
			for (int i = 1; i <= s1.length(); i++) {
				for (int j = 1; j <= s2.length(); j++) {
					int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
					dp[i][j] = Math.min(
						Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
						dp[i - 1][j - 1] + cost
					);
				}
			}

			return dp[s1.length()][s2.length()];
		}

		/**
		 * OCR 결과 유사성 판단 메서드
		 * @param existingText 기존 텍스트
		 * @param newText 새 텍스트
		 * @return 유사성 여부
		 */
		public static boolean isOcrResultSimilar(String existingText, String newText) {
			// null 체크
			if (existingText == null || newText == null) {
				return false;
			}

			// 길이 차이가 큰 경우 다른 텍스트로 판단
			if (Math.abs(existingText.length() - newText.length()) > 2) {
				return false;
			}

			// Levenshtein Distance 계산
			int distance = calculateLevenshteinDistance(existingText, newText);

			// 문자열 길이에 따른 유사성 임계값 계산
			int maxLength = Math.max(existingText.length(), newText.length());
			double similarityThreshold;

			if (maxLength <= 3) {
				// 짧은 문자열의 경우 더 엄격한 기준
				similarityThreshold = 0.33; // 1/3
			} else if (maxLength <= 5) {
				similarityThreshold = 0.4; // 2/5
			} else {
				// 긴 문자열의 경우 좀 더 관대한 기준
				similarityThreshold = 0.5; // 1/2
			}

			// 정규화된 편집 거리 계산
			double normalizedDistance = (double) distance / maxLength;

			return normalizedDistance <= similarityThreshold;
		}
	}

	private List<String> previousOcrResults = new ArrayList<>();

	public void processScanResults(BookReturnRequest bookReturnRequest) {
		shelfBookItemsMap = bookReturnRequest.getShelfBookItemsMap();
		Integer status = shelfBookItemsMap.get(0).get(0);

		if(status != 0) {
			notificationService.addCameraErrorNotification(NotificationInformation.CAMERA_ERROR);
			throw new BookHistoryException(ErrorCode.CAMERA_ERROR);
		}

		// 1. Shelf별로 도서 반납 처리 및 카테고리 검증
		List<BookItemEntity> returnedBooks = processReturnsByShelf(
			shelfBookItemsMap
		);

		// 2. OCR 결과 분석
		List<String> newOcrTexts = analyzeOcrChanges(
			bookReturnRequest.getOcrResults()
		);

		// 3. 반납된 도서가 없고 새로운 OCR 결과가 있다면 관리자에게 알림
		if (returnedBooks.isEmpty() && !newOcrTexts.isEmpty()) {
			for(String newOcrText : newOcrTexts) {
				notificationService.addQrScanErrorNotification(NotificationInformation.QR_SCAN_ERROR, newOcrText);
			}
		}
	}

	private List<BookItemEntity> processReturnsByShelf(Map<Integer, List<Integer>> shelfBookItemsMap) {
		if (shelfBookItemsMap.isEmpty()) {
			return new ArrayList<>();
		}

		List<BookItemEntity> allReturnedBooks = new ArrayList<>();

		shelfBookItemsMap.entrySet().stream()
			.filter(entry -> entry.getKey() != 0)
			.forEach(entry -> {
				List<BookItemEntity> shelfBooks = validateAndReturnBooks(entry.getKey(), entry.getValue());
				allReturnedBooks.addAll(shelfBooks);
			});

		allReturnedBooks.forEach(bookItem ->
			notificationService.addFavoriteBookAvailableNotification(
				NotificationInformation.FAVORITE_BOOK_AVAILABLE,
				bookItem.getBookInformation().getTitle(),
				bookItem.getId()
			)
		);

		return allReturnedBooks;
	}

	private List<BookItemEntity> validateAndReturnBooks(Integer shelfId, List<Integer> bookItemIds) {
		if (bookItemIds == null || bookItemIds.isEmpty()) {
			return new ArrayList<>();
		}

		ShelfEntity shelf = shelfRepository.findById(shelfId)
			.orElseThrow(() -> new ShelfException(ErrorCode.SHELF_NOT_FOUND));

		List<BookItemEntity> allBooks = bookItemRepository.findAllById(bookItemIds);

		mismatchedBooks = allBooks.stream()
			.filter(book -> !Objects.equals(book.getBookInformation().getCategory(), shelf.getCategory()))
			.collect(Collectors.toList());

		List<BookItemEntity> borrowedBooks = allBooks.stream()
			.filter(book -> book.getBookStatus() == BookStatus.BORROWED && !book.getDeleted())
			.collect(Collectors.toList());

		borrowedBooks.forEach(this::processReturn);

		return borrowedBooks;
	}

	public void mismatchedBooksAlarms() {
		if(!mismatchedBooks.isEmpty()) {
			String titles = mismatchedBooks.stream()
				.map(book -> book.getBookInformation().getTitle())
				.collect(Collectors.joining(", "));
			notificationService.addBooKArrangementNotification(NotificationInformation.BOOK_ARRANGEMENT, titles);
		}
	}

	public void lostBooksAlarms() {
		List<Integer> bookItemIds = shelfBookItemsMap.entrySet().stream()
			.filter(entry -> entry.getKey() != 0)
			.flatMap(entry -> entry.getValue().stream())
			.collect(Collectors.toList());

		List<Integer> availableBookItemIds = bookItemRepository.findIdsByBookStatus(BookStatus.AVAILABLE);

		// 성능을 위해 Set으로 변환
		Set<Integer> availableIdsSet = new HashSet<>(availableBookItemIds);

		// availableIdsSet에 없는 bookItemIds를 찾음 (= 분실 의심 도서)
		List<Integer> lostBookItemIds = bookItemIds.stream()
			.filter(id -> !availableIdsSet.contains(id))
			.collect(Collectors.toList());

		notificationService.addLostBookNotification(lostBookItemIds);
	}

	private void processReturn(BookItemEntity bookItemEntity) {
		bookItemEntity.returnBook();

		BookHistoryEntity latestHistory = bookItemEntity.getBookHistories().stream()
			.filter(history -> history.getReturnedAt() == null)
			.max(Comparator.comparing(BookHistoryEntity::getBorrowedAt))
			.orElseThrow(() -> new BookItemException(ErrorCode.INVALID_BOOK_STATUS));

		latestHistory.returnBook();

	}

	private List<String> analyzeOcrChanges(List<String> currentOcrResults) {
		if (currentOcrResults == null || currentOcrResults.isEmpty()) {
			return new ArrayList<>();
		}

		List<String> newOcrTexts = new ArrayList<>();

		if (currentOcrResults.size() > previousOcrResults.size()) {
			newOcrTexts = findNewOcrTexts(currentOcrResults);
		}
		this.previousOcrResults = new ArrayList<>(currentOcrResults);
		return newOcrTexts;
	}

	private List<String> findNewOcrTexts(List<String> currentOcrResults) {
		return currentOcrResults.stream()
			.filter(currentText ->
				// 기존 OCR 결과와 유사하지 않은 텍스트만 필터링
				previousOcrResults.stream()
					.noneMatch(existingText ->
						OcrSimilarityAnalyzer.isOcrResultSimilar(existingText, currentText)
					)
			)
			.collect(Collectors.toList());
	}
}