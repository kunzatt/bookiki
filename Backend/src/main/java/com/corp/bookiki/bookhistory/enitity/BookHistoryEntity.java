package com.corp.bookiki.bookhistory.enitity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;
import com.corp.bookiki.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_item_id")
	private BookItemEntity bookItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(nullable = false)
	private LocalDateTime borrowedAt;

	private LocalDateTime returnedAt;

	@Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
	private Boolean overdue = false;

	@Builder
	public BookHistoryEntity(BookItemEntity bookItem, UserEntity user, LocalDateTime borrowedAt, LocalDateTime returnedAt, Boolean overdue) {
		this.bookItem = bookItem;
		this.user = user;
		this.borrowedAt = borrowedAt;
		this.returnedAt = returnedAt;
		this.overdue = overdue;
	}

	public static BookHistoryEntity createForBorrow(BookItemEntity bookItem, UserEntity user) {
		return BookHistoryEntity.builder()
			.bookItem(bookItem)
			.user(user)
			.borrowedAt(LocalDateTime.now())
			.returnedAt(null)
			.build();
	}

	public void returnBook(LoanPolicyEntity loanPolicy) {
		this.returnedAt = LocalDateTime.now();
		checkAndSetOverdue(loanPolicy);
	}

	private void checkAndSetOverdue(LoanPolicyEntity loanPolicy) {
		long daysBetween = ChronoUnit.DAYS.between(borrowedAt, returnedAt);
		this.overdue = daysBetween > loanPolicy.getLoanPeriod();
	}
}
