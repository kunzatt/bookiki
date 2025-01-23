package com.corp.bookiki.bookinformation.service;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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

	// 책 정보 추가
	@Transactional
	public BookInformationResponse addBookInformationByISBN(String isbn) {
		BookInformationEntity bookInfo = getBookInformationByISBN(isbn);

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

	// 외부 API를 이용한 책 정보 가져오기
	private BookInformationEntity callExternalBookApi(String isbn) {
		String url = new StringBuilder()
			.append(API_URL)
			.append("?cert_key=").append(API_NL_KEY)
			.append("&result_style=json")
			.append("&page_no=1")
			.append("&page_size=1")
			.append("&isbn=").append(isbn)
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

			String title = bookData.getString("TITLE");
			String author = bookData.getString("AUTHOR");
			String publisher = bookData.optString("PUBLISHER");
			String image = bookData.optString("TITLE_URL");
			String description = bookData.optString("BOOK_INTRODUCTION");
			String category = bookData.optString("DDC");

			publisher = publisher.isEmpty() ? null : publisher;
			image = image.isEmpty() ? null : image;
			description = description.isEmpty() ? null : description;
			category = category.isEmpty() ? null : category;

			String publishDate = bookData.getString("PUBLISH_PREDATE");
			LocalDateTime publishedAt = LocalDateTime.parse(publishDate + "T00:00:00");

			return BookInformationEntity.builder()
				.title(title)
				.author(author)
				.publisher(publisher)
				.isbn(isbn)
				.publishedAt(publishedAt)
				.image(image)
				.description(description)
				.category(category)
				.build();

		} catch (Exception e) {
			throw new BookInformationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	// 책 정보 가져오기
	private BookInformationEntity getBookInformationByISBN(String isbn) {
		return bookInformationRepository.findByIsbn(isbn);
	}
}
