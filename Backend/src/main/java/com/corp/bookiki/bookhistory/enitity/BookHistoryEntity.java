package com.corp.bookiki.bookhistory.enitity;

import java.time.LocalDateTime;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
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

	public BookHistoryEntity(BookItemEntity bookItem, UserEntity user) {
		this.bookItem = bookItem;
		this.user = user;
		this.borrowedAt = LocalDateTime.now();
	}

	public void returnBook() {
		this.returnedAt = LocalDateTime.now();
	}
}
