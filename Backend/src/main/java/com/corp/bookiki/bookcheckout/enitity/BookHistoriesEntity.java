package com.corp.bookiki.bookcheckout.enitity;

import java.awt.print.Book;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookHistoriesEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_items")
	private BookItemEntity bookItemId;

	private Integer userId;

	private LocalDateTime borrowedAt;

	private LocalDateTime returnedAt;

}
