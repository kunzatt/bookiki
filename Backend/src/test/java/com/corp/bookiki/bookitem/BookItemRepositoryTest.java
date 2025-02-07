package com.corp.bookiki.bookitem;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.bookitem.repository.BookItemRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class BookItemRepositoryTest {
	@Autowired
	private BookItemRepository bookItemRepository;

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

	private BookItemEntity createBookItem(boolean deleted) {
		return BookItemEntity.builder()
			.purchaseAt(LocalDateTime.now())
			.bookStatus(BookStatus.AVAILABLE)
			.deleted(deleted)
			.build();
	}
}