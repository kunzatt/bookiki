package com.corp.bookiki.bookinformation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.corp.bookiki.bookinformation.dto.BookInformationResponse;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookinformation.repository.BookInformationRepository;
import com.corp.bookiki.bookinformation.service.BookInformationService;
import com.corp.bookiki.global.error.exception.BookInformationException;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
class BookInformationServiceTest {

	@InjectMocks
	private BookInformationService bookInformationService;

	@Mock
	private BookInformationRepository bookInformationRepository;

	@Mock
	private RestTemplate restTemplate;

	@Nested
	@DisplayName("도서 정보 추가 테스트")
	class AddBookInformation {

		@Test
		@DisplayName("이미 존재하는 도서 정보 조회 시 성공")
		void addBookInformation_WhenExists_ThenSuccess() {
			String isbn = "9788937460470";
			BookInformationEntity existingBook = createBookEntity(isbn);

			given(bookInformationRepository.findByIsbn(isbn)).willReturn(existingBook);

			var result = bookInformationService.addBookInformationByIsbn(isbn);
			assertEquals(isbn, result.getIsbn());
			verify(bookInformationRepository, never()).save(any());
		}

		@Test
		@DisplayName("외부 API를 통한 새로운 도서 정보 추가 성공")
		void addBookInformation_WhenNotExists_ThenSuccess() {
			String isbn = "9788937460470";
			String mockResponse = createMockApiResponse();

			given(bookInformationRepository.findByIsbn(isbn)).willReturn(null);
			given(restTemplate.getForEntity(anyString(), eq(String.class)))
				.willReturn(ResponseEntity.ok(mockResponse));

			assertDoesNotThrow(() ->
				bookInformationService.addBookInformationByIsbn(isbn));
			verify(bookInformationRepository).save(any());
		}
	}

	@Test
	@DisplayName("id로 도서 정보 조회 성공")
	void getBookInformation_WhenExists_ThenSuccess() {
		int id = 1;
		BookInformationEntity entity = createBookEntity("9788937460470");

		given(bookInformationRepository.findById(id)).willReturn(Optional.of(entity));

		BookInformationResponse result = bookInformationService.getBookInformation(id);
		assertNotNull(result);
		assertEquals(entity.getTitle(), result.getTitle());
	}

	@Test
	@DisplayName("존재하지 않는 id로 조회 시 예외 발생")
	void getBookInformation_WhenNotExists_ThenThrowException() {
		int id = Integer.MAX_VALUE;
		given(bookInformationRepository.findById(id)).willReturn(Optional.empty());

		assertThrows(BookInformationException.class,
			() -> bookInformationService.getBookInformation(id));
	}

	private BookInformationEntity createBookEntity(String isbn) {
		return BookInformationEntity.builder()
			.isbn(isbn)
			.title("테스트 도서")
			.author("테스트 저자")
			.publishedAt(LocalDateTime.now())
			.build();
	}

	private String createMockApiResponse() {
		return """
			{
			    "TOTAL_COUNT": "1",
			    "docs": [{
			        "TITLE": "테스트 도서",
			        "AUTHOR": "테스트 저자",
			        "PUBLISHER": "테스트 출판사",
			        "PUBLISH_PREDATE": "20240101"
			    }]
			}
			""";
	}
}