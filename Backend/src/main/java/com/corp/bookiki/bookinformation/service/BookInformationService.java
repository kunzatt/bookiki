package com.corp.bookiki.bookinformation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.corp.bookiki.recommendation.service.GeminiService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.corp.bookiki.bookinformation.dto.BookInformationResponse;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookinformation.repository.BookInformationRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookInformationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 주요 기능: 책 정보 추가, 책 정보 가져오기, 외부 API를 이용한 책 정보 가져오기
@Service
@RequiredArgsConstructor
@Slf4j
public class BookInformationService {

	@Value("${naver.api.books-client-id}")
	private String clientId;

	@Value("${naver.api.books-client-secret}")
	private String clientSecret;

	private static final String API_URL = "https://openapi.naver.com/v1/search/book.json";

	private final RestTemplate restTemplate;
	private final BookInformationRepository bookInformationRepository;
	private final ObjectMapper objectMapper;
	private final GeminiService geminiService;

	// 책 정보를 추가하는 메서드
	@Transactional
	public BookInformationResponse addBookInformationByIsbn(String isbn) {
		BookInformationEntity bookInfo = getBookInformationByIsbn(isbn);

		if (bookInfo != null) {
			return BookInformationResponse.from(bookInfo);
		}
		try {
			BookInformationEntity newBookInfo = callNaverBookApi(isbn);
			geminiService.getCategoryForBook(newBookInfo);
			bookInformationRepository.save(newBookInfo); // 새로운 BookInformation 저장
			return BookInformationResponse.from(newBookInfo);
		} catch (BookInformationException e) {
			throw new BookInformationException(ErrorCode.BOOK_INFO_NOT_FOUND);
		}
	}

	// 네이버 API를 이용하여 책 정보 가져오는 메서드
	private BookInformationEntity callNaverBookApi(String isbn) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Naver-Client-Id", clientId);
		headers.set("X-Naver-Client-Secret", clientSecret);

		String url = UriComponentsBuilder.fromHttpUrl(API_URL)
			.queryParam("query", isbn)
			.queryParam("display", 1)
			.build()
			.toString();

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(headers),
				String.class
			);

			// 디버깅을 위한 응답 출력
			log.debug("Naver API Response: {}", response.getBody());

			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode items = root.get("items");

			// 검색 결과 확인
			if (items == null || items.isEmpty()) {
				log.error("No items found for ISBN: {}", isbn);
				throw new BookInformationException(ErrorCode.BOOK_INFO_NOT_FOUND);
			}

			JsonNode bookData = items.get(0);

			// ISBN 확인 로직 개선
			String returnedIsbn = bookData.get("isbn").asText().replace(" ", "");
			if (!returnedIsbn.contains(isbn)) {
				log.error("ISBN mismatch. Requested: {}, Returned: {}", isbn, returnedIsbn);
				throw new BookInformationException(ErrorCode.BOOK_INFO_NOT_FOUND);
			}

			String pubdate = bookData.get("pubdate").asText();
			LocalDateTime publishedAt;
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				publishedAt = LocalDate.parse(pubdate, formatter).atStartOfDay();
			} catch (Exception e) {
				log.error("Failed to parse publication date: {}", pubdate, e);
				publishedAt = LocalDateTime.now(); // 또는 다른 기본값 설정
			}

			return BookInformationEntity.builder()
				.title(bookData.get("title").asText().replaceAll("<[^>]*>", ""))
				.author(bookData.get("author").asText().replaceAll("<[^>]*>", ""))
				.publisher(StringUtils.defaultIfEmpty(bookData.get("publisher").asText(), null))
				.isbn(isbn)
				.publishedAt(publishedAt)
				.image(StringUtils.defaultIfEmpty(bookData.get("image").asText(), null))
				.description(StringUtils.defaultIfEmpty(bookData.get("description").asText().replaceAll("<[^>]*>", ""), null))
				.build();

		} catch (BookInformationException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to call Naver Book API", e);
			throw new BookInformationException(ErrorCode.EXTERNAL_API_ERROR);
		}
	}

	// Isbn을 이용하여 책 정보를 가져오는 메서드
	private BookInformationEntity getBookInformationByIsbn(String isbn) {
		return bookInformationRepository.findByIsbn(isbn);
	}

	// BookInformation의 id를 이용하여 책 정보를 가져오는 메서드
	public BookInformationResponse getBookInformation(int id) {
		BookInformationEntity bookInfo = bookInformationRepository.findById(id)
			.orElseThrow(() -> new BookInformationException(ErrorCode.BOOK_INFO_NOT_FOUND));
		return BookInformationResponse.from(bookInfo);
	}
}
