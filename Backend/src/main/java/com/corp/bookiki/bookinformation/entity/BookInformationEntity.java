package com.corp.bookiki.bookinformation.entity;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "book_informations",
	indexes = {
		@Index(name = "idx_title", columnList = "title"),
		@Index(name = "idx_author", columnList = "author"),
		@Index(name = "idx_isbn", columnList = "isbn")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BookInformationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, length = 100)
	private String author;

	@Column(length = 100)
	private String publisher;

	@Column(unique = true, nullable = false, length = 100)
	private String isbn;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Column(length = 255)
	private String image;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(length = 10)
	private Integer category;

	@Builder
	public BookInformationEntity(String title, String author, String publisher, String isbn, LocalDateTime publishedAt,
		String image, String description, Integer category) {
		this.title = title;
		this.author = author;
		this.publisher = publisher;
		this.isbn = isbn;
		this.publishedAt = publishedAt;
		this.image = image;
		this.description = description;
		this.category = category;
	}
}