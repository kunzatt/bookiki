package com.corp.bookiki.bookhistory;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;

@DataJpaTest
@ActiveProfiles("test")
class BookHistoryRepositoryTest {

	@Autowired
	private BookHistoryRepository bookHistoryRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void testFindByBorrowedAtBetweenWithLimit() {
		UserEntity user = createTestUser();
		BookItemEntity bookItem = new BookItemEntity();
		entityManager.persist(bookItem);

		LocalDateTime now = LocalDateTime.now();
		BookHistoryEntity history1 = createBookHistoryWithBorrowedAt(bookItem, user, now.minusDays(1));
		BookHistoryEntity history2 = createBookHistoryWithBorrowedAt(bookItem, user, now);

		entityManager.persist(history1);
		entityManager.persist(history2);
		entityManager.flush();

		List<BookHistoryResponse> results = bookHistoryRepository.findByBorrowedAtBetween(
			now.minusDays(2),
			now,
			Limit.of(1)
		);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getBookItemId()).isEqualTo(bookItem.getId());
	}

	@Test
	void testFindByBorrowedAtBetweenWithPagination() {
		UserEntity user = createTestUser();
		BookItemEntity bookItem = new BookItemEntity();
		entityManager.persist(bookItem);

		LocalDateTime now = LocalDateTime.now();
		for (int i = 0; i < 5; i++) {
			BookHistoryEntity history = createBookHistoryWithBorrowedAt(bookItem, user, now.minusDays(i));
			entityManager.persist(history);
		}
		entityManager.flush();

		Page<BookHistoryEntity> resultPage = bookHistoryRepository.findByBorrowedAtBetween(
			now.minusDays(5),
			now,
			PageRequest.of(0, 2)
		);

		assertThat(resultPage.getContent()).hasSize(2);
		assertThat(resultPage.getTotalElements()).isEqualTo(5);
	}

	private UserEntity createTestUser() {
		UserEntity user = UserEntity.builder()
			.email("test@example.com")
			.password("password")
			.userName("Test User")
			.companyId("TEST001")
			.role(Role.USER)
			.provider(Provider.BOOKIKI)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		entityManager.persist(user);
		return user;
	}

	private BookHistoryEntity createBookHistoryWithBorrowedAt(
		BookItemEntity bookItem,
		UserEntity user,
		LocalDateTime borrowedAt
	) {
		try {
			BookHistoryEntity history = new BookHistoryEntity(bookItem, user);
			Field field = BookHistoryEntity.class.getDeclaredField("borrowedAt");
			field.setAccessible(true);
			field.set(history, borrowedAt);
			return history;
		} catch (Exception e) {
			throw new RuntimeException("Failed to set borrowedAt", e);
		}
	}
}