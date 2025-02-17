package com.corp.bookiki.bookhistory;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class BookHistoryRepositoryTest {

	@Autowired
	private BookHistoryRepository bookHistoryRepository;

	@Autowired
	private EntityManager entityManager;

	private BookInformationEntity bookInformation;
	private BookItemEntity bookItem;
	private UserEntity user;
	private BookHistoryEntity bookHistory;
	private LoanPolicyEntity loanPolicy;
	private LocalDateTime now;

	@BeforeEach
	void setUp() {
		now = LocalDateTime.now();

		bookInformation = BookInformationEntity.builder()
			.title("Spring Boot in Action")
			.author("Craig Walls")
			.isbn("978-1617292545")
			.publisher("Manning")
			.build();

		bookItem = BookItemEntity.builder()
			.bookInformation(bookInformation)
			.purchaseAt(LocalDateTime.now())
			.bookStatus(BookStatus.AVAILABLE)
			.updatedAt(LocalDateTime.now())
			.build();

		user = UserEntity.builder()
			.email("test@bookiki.com")
			.password("password123")
			.userName("Test User")
			.companyId("EMP001")
			.role(Role.USER)
			.provider(Provider.BOOKIKI)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		bookHistory = BookHistoryEntity.builder()
			.bookItem(bookItem)
			.user(user)
			.borrowedAt(now)
			.returnedAt(null)
			.build();

		loanPolicy = LoanPolicyEntity.builder().loanPeriod(7).build();
	}

	@Test
	void searchBookHistoryWithCount() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);
		entityManager.flush();

		bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		LocalDateTime startDate = now.minusDays(1);
		LocalDateTime endDate = now.plusDays(1);
		String keyword = "Spring";
		PageRequest pageRequest = PageRequest.of(0, 10);

		Page<BookHistoryEntity> result = bookHistoryRepository.searchBookHistoryWithCount(startDate, endDate, keyword,
			pageRequest);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent().get(0).getBorrowedAt()).isAfterOrEqualTo(startDate).isBeforeOrEqualTo(endDate);
	}

	@Test
	void findCurrentBorrowByBookItem() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);
		entityManager.flush();

		bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		var result = bookHistoryRepository.findCurrentBorrowByBookItem(bookItem);

		assertThat(result).isPresent();
		assertThat(result.get().getBookItem().getId()).isEqualTo(bookItem.getId());
		assertThat(result.get().getReturnedAt()).isNull();
	}

	@Test
	void findCurrentBorrowsByUser() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);
		entityManager.flush();

		bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		List<BookHistoryEntity> result = bookHistoryRepository.findCurrentBorrowsByUser(user);

		assertThat(result.get(0).getUser().getEmail()).isEqualTo(user.getEmail());
		assertThat(result.get(0).getReturnedAt()).isNull();
	}

	@Test
	void findOverdueBooks() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);
		entityManager.flush();

		LocalDateTime overdueDate = now.minusDays(15);
		bookHistory.returnBook();
		bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		List<BookHistoryEntity> result = bookHistoryRepository.findOverdueBooks(overdueDate);

		assertThat(result).isEmpty();
	}

	@Test
	void findMostBorrowedBooks() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);
		entityManager.flush();

		bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		LocalDateTime startDate = now.minusDays(30);
		LocalDateTime endDate = now.plusDays(1);
		PageRequest pageRequest = PageRequest.of(0, 10);

		List<Object[]> result = bookHistoryRepository.findMostBorrowedBooks(startDate, endDate, pageRequest);

		assertThat(result).isNotEmpty();
	}

	@Test
	void searchUserBookHistoryWithCount() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);
		entityManager.flush();

		bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		LocalDateTime startDate = now.minusDays(1);
		LocalDateTime endDate = now.plusDays(1);
		String keyword = "Spring";
		PageRequest pageRequest = PageRequest.of(0, 10);

		Page<BookHistoryEntity> result = bookHistoryRepository.searchUserBookHistoryWithCount(user.getId(), startDate,
			endDate, keyword, pageRequest);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(user.getId());
	}

	@Test
	void findByBorrowedAtBetween() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);
		entityManager.flush();

		bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		LocalDateTime startDate = now.minusDays(1);
		LocalDateTime endDate = now.plusDays(1);
		Limit limit = Limit.of(10);

		List<BookHistoryEntity> result = bookHistoryRepository.findByBorrowedAtBetween(startDate, endDate, limit);

		assertThat(result).isNotEmpty();
		assertThat(result.get(0).getBorrowedAt()).isAfterOrEqualTo(startDate).isBeforeOrEqualTo(endDate);
	}

	@Test
	void findAllForAdmin() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);

		bookHistory = BookHistoryEntity.builder()
			.bookItem(bookItem)
			.user(user)
			.borrowedAt(now)
			.returnedAt(null)
			.overdue(false)
			.build();

		entityManager.persist(bookHistory);

		entityManager.flush();
		entityManager.clear();

		LocalDateTime startDate = now.minusMinutes(1);
		LocalDateTime endDate = now.plusMinutes(1);
		PageRequest pageRequest = PageRequest.of(0, 10);

		Page<BookHistoryEntity> result = bookHistoryRepository.findAllBookHistoriesForAdmin(startDate, endDate, user.getUserName(),
			user.getCompanyId(), false, pageRequest);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isNotEmpty();
	}

	@Test
	void findAllForUser() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);

		bookHistory = BookHistoryEntity.builder()
			.bookItem(bookItem)
			.user(user)
			.borrowedAt(now)
			.returnedAt(null)
			.overdue(false)
			.build();

		bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		LocalDateTime startDate = now.minusMinutes(1);
		LocalDateTime endDate = now.plusMinutes(1);
		PageRequest pageRequest = PageRequest.of(0, 10);

		Page<BookHistoryEntity> result = bookHistoryRepository.findAllForUser(user.getId(), startDate, endDate,
			false, pageRequest);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent().get(0).getUser().getEmail()).isEqualTo(user.getEmail());
	}

	@Test
	void findCurrentBorrowsByUserId() {
		entityManager.persist(bookInformation);
		entityManager.persist(user);
		entityManager.persist(bookItem);

		bookHistory = BookHistoryEntity.builder()
			.bookItem(bookItem)
			.user(user)
			.borrowedAt(now)
			.returnedAt(null)
			.overdue(false)
			.build();

		bookHistory = bookHistoryRepository.save(bookHistory);
		entityManager.flush();
		entityManager.clear();

		List<BookHistoryEntity> result = bookHistoryRepository.findCurrentBorrowsByUserId(user.getId(), null);

		assertThat(result).isNotEmpty();
		assertThat(result.get(0).getUser().getEmail()).isEqualTo(user.getEmail());
		assertThat(result.get(0).getReturnedAt()).isNull();
	}
}