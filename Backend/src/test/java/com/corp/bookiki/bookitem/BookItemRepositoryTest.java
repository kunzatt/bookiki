package com.corp.bookiki.bookitem;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookinformation.repository.BookInformationRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class BookItemRepositoryTest {
	@Autowired
	private BookItemRepository bookItemRepository;
	@Autowired
	private BookInformationRepository bookInformationRepository;

	@Test
	void findByDeletedFalse_ShouldReturnOnlyNonDeletedItems() {
		// given
		BookItemEntity bookItem1 = createBookItem(false);
		BookItemEntity bookItem2 = createBookItem(true);

		bookItemRepository.save(bookItem1);
		bookItemRepository.save(bookItem2);
		log.info("테스트 도서 아이템 저장 완료: id1={}, id2={}", bookItem1.getId(), bookItem2.getId());

		// when
		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("id").descending());
		Page<BookItemEntity> result = bookItemRepository.findByDeletedFalse(pageRequest);
		log.info("삭제되지 않은 도서 아이템 조회 결과: count={}", result.getTotalElements());

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getDeleted()).isFalse();
		log.info("도서 아이템 조회 테스트 성공");
	}

	@Test
	void findAllWithKeyword_ShouldSearchTitleAndDescription() {
		// given
		BookInformationEntity bookInfo = BookInformationEntity.builder()
				.title("Test Title")
				.author("Author")
				.description("Test Description")
				.isbn("1234567890")
				.build();

		bookInfo = bookInformationRepository.save(bookInfo);

		BookItemEntity bookItem = BookItemEntity.builder()
				.bookInformation(bookInfo)
				.deleted(false)
				.build();
		bookItemRepository.save(bookItem);

		PageRequest pageRequest = PageRequest.of(0, 10);

		// then
		assertThat(bookItemRepository.findAllWithKeyword("Test", pageRequest).getContent()).hasSize(1);
		assertThat(bookItemRepository.findAllWithKeyword("Description", pageRequest).getContent()).hasSize(1);
		assertThat(bookItemRepository.findAllWithKeyword("test", pageRequest).getContent()).hasSize(1);
		assertThat(bookItemRepository.findAllWithKeyword(null, pageRequest).getContent()).hasSize(1);
		assertThat(bookItemRepository.findAllWithKeyword("NonExistent", pageRequest).getContent()).isEmpty();
	}

	@Test
	void findAllWithKeyword_ShouldSortResults() {
		// given
		BookInformationEntity bookInfo1 = BookInformationEntity.builder()
				.title("A Test Title")
				.author("A Test Author")
				.description("Description")
				.isbn("1234567890")
				.build();

		BookInformationEntity bookInfo2 = BookInformationEntity.builder()
				.title("B Test Title")
				.author("B Test Author")
				.description("Description")
				.isbn("0987654321")
				.build();

		bookInfo1 = bookInformationRepository.save(bookInfo1);
		bookInfo2 = bookInformationRepository.save(bookInfo2);

		BookItemEntity bookItem1 = BookItemEntity.builder()
				.bookInformation(bookInfo1)
				.deleted(false)
				.build();

		BookItemEntity bookItem2 = BookItemEntity.builder()
				.bookInformation(bookInfo2)
				.deleted(false)
				.build();

		bookItemRepository.saveAll(List.of(bookItem1, bookItem2));

		// then
		PageRequest ascPageRequest = PageRequest.of(0, 10, Sort.by("bookInformation.title").ascending());
		Page<BookItemEntity> ascResult = bookItemRepository.findAllWithKeyword("Test", ascPageRequest);
		assertThat(ascResult.getContent()).hasSize(2);
		assertThat(ascResult.getContent().get(0).getBookInformation().getTitle()).isEqualTo("A Test Title");

		PageRequest descPageRequest = PageRequest.of(0, 10, Sort.by("bookInformation.title").descending());
		Page<BookItemEntity> descResult = bookItemRepository.findAllWithKeyword("Test", descPageRequest);
		assertThat(descResult.getContent()).hasSize(2);
		assertThat(descResult.getContent().get(0).getBookInformation().getTitle()).isEqualTo("B Test Title");
	}

	@Test
	void searchBooks_ShouldTestAllSearchTypes() {
		// given
		BookInformationEntity bookInfo = BookInformationEntity.builder()
				.title("Test Title")
				.author("Test Author")
				.publisher("Test Publisher")
				.description("Test Description")
				.isbn("1234567890")
				.build();

		bookInfo = bookInformationRepository.save(bookInfo);

		BookItemEntity bookItem = BookItemEntity.builder()
				.bookInformation(bookInfo)
				.deleted(false)
				.build();
		bookItemRepository.save(bookItem);

		PageRequest pageRequest = PageRequest.of(0, 10);

		// then
		assertThat(bookItemRepository.searchBooks("TITLE", "Test", pageRequest).getContent()).hasSize(1);
		assertThat(bookItemRepository.searchBooks("AUTHOR", "Test", pageRequest).getContent()).hasSize(1);
		assertThat(bookItemRepository.searchBooks("PUBLISHER", "Test", pageRequest).getContent()).hasSize(1);
		assertThat(bookItemRepository.searchBooks("KEYWORD", "Test", pageRequest).getContent()).hasSize(1);
		assertThat(bookItemRepository.searchBooks("TITLE", "test", pageRequest).getContent()).hasSize(1);
	}

	private BookItemEntity createBookItem(boolean deleted) {
		return BookItemEntity.builder()
			.purchaseAt(LocalDateTime.now())
			.bookStatus(BookStatus.AVAILABLE)
			.deleted(deleted)
			.build();
	}
}