package com.corp.bookiki.bookinformation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookinformation.repository.BookInformationRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class BookInformationRepositoryTest {

	@Autowired
	private BookInformationRepository bookInformationRepository;

	@Test
	@DisplayName("ISBN으로 도서 정보 조회 성공")
	void findByIsbn_WhenExists_ThenReturnBook() {
		String isbn = "9788937460470";
		BookInformationEntity book = BookInformationEntity.builder()
			.isbn(isbn)
			.title("테스트 도서")
			.author("테스트 저자")
			.publishedAt(LocalDateTime.now())
			.build();

		BookInformationEntity savedBook = bookInformationRepository.save(book);
		log.info("테스트 도서 저장: id={}, isbn={}", savedBook.getId(), savedBook.getIsbn());

		BookInformationEntity found = bookInformationRepository.findByIsbn(isbn);
		log.info("도서 조회 결과: found={}", found != null);

		assertThat(found).isNotNull();
		assertThat(found.getIsbn()).isEqualTo(isbn);
	}

	@Test
	@DisplayName("존재하지 않는 ISBN으로 조회 시 null 반환")
	void findByIsbn_WhenNotExists_ThenReturnNull() {
		String isbn = "9788937460470";

		BookInformationEntity found = bookInformationRepository.findByIsbn(isbn);
		log.info("존재하지 않는 도서 조회: isbn={}, found={}", isbn, found != null);

		assertThat(found).isNull();
	}

	@Test
	@DisplayName("id로 도서 정보 조회 성공")
	void findById_WhenExists_ThenReturnBook() {
		BookInformationEntity book = BookInformationEntity.builder()
			.title("테스트 도서")
			.author("테스트 저자")
			.isbn("9788937460470")
			.build();

		BookInformationEntity savedBook = bookInformationRepository.save(book);
		Optional<BookInformationEntity> found = bookInformationRepository.findById(savedBook.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getTitle()).isEqualTo(book.getTitle());
	}
}