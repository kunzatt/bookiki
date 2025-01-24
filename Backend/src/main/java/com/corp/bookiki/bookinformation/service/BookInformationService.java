package com.corp.bookiki.bookinformation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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

import lombok.RequiredArgsConstructor;

// 주요 기능: 책 정보 추가, 책 정보 가져오기, 외부 API를 이용한 책 정보 가져오기
@Service
@RequiredArgsConstructor
public class BookInformationService {

	@Value("${api.nl.key}")
	private String API_NL_KEY;

	private static final String API_URL = "https://www.nl.go.kr/seoji/SearchApi.do";

	private final RestTemplate restTemplate;

	private final BookInformationRepository bookInformationRepository;

	// 책 정보를 추가하는 메서드
	@Transactional
	public BookInformationResponse addBookInformationByIsbn(String isbn) {
		BookInformationEntity bookInfo = getBookInformationByIsbn(isbn);

		if (bookInfo != null) {
			return BookInformationResponse.from(bookInfo);
		}
		try {
			BookInformationEntity newBookInfo = callExternalBookApi(isbn);
			bookInformationRepository.save(newBookInfo); // 새로운 BookInformation 저장
			return BookInformationResponse.from(newBookInfo);
		} catch (BookInformationException e) {
			throw new BookInformationException(ErrorCode.BOOK_INFO_NOT_FOUND);
		}
	}

	// 외부 API를 이용하여 책 정보 가져오는 메서드
	private BookInformationEntity callExternalBookApi(String isbn) {
		String url = UriComponentsBuilder.fromHttpUrl(API_URL)
			.queryParam("cert_key", API_NL_KEY)
			.queryParam("result_style", "json")
			.queryParam("page_no", 1)
			.queryParam("page_size", 1)
			.queryParam("isbn", isbn)
			.build()
			.toString();

		try {
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			JSONObject jsonResponse = new JSONObject(response.getBody());

			if (jsonResponse.has("ERR_CODE")) {
				throw new BookInformationException(ErrorCode.EXTERNAL_API_ERROR);
			}

			if (jsonResponse.getString("TOTAL_COUNT").equals("0")) {
				throw new BookInformationException(ErrorCode.INVALID_ISBN);
			}

			JSONObject bookData = jsonResponse.getJSONArray("docs").getJSONObject(0);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDateTime publishedAt = LocalDate.parse(bookData.getString("PUBLISH_PREDATE"), formatter)
				.atStartOfDay();

			return BookInformationEntity.builder()
				.title(bookData.getString("TITLE"))
				.author(bookData.getString("AUTHOR"))
				.publisher(StringUtils.defaultIfEmpty(bookData.optString("PUBLISHER"), null))
				.isbn(isbn)
				.publishedAt(publishedAt)
				.image(StringUtils.defaultIfEmpty(bookData.optString("TITLE_URL"), null))
				.description(StringUtils.defaultIfEmpty(bookData.optString("BOOK_INTRODUCTION"), null))
				.category(StringUtils.defaultIfEmpty(bookData.optString("DDC"), null))
				.build();

		} catch (Exception e) {
			throw new BookInformationException(ErrorCode.INTERNAL_SERVER_ERROR);
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
