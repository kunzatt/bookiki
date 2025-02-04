package com.corp.bookiki.favorite.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "favorites",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_user_book",
			columnNames = {"user_id", "book_item_id"}
		)
	},
	indexes = {
		@Index(name = "idx_user_id", columnList = "user_id"),
		@Index(name = "idx_book_item_id", columnList = "book_item_id")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_item_id", nullable = false)
	private BookItemEntity bookItem;

	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public static FavoriteEntity create(UserEntity user, BookItemEntity bookItem) {
		FavoriteEntity favorite = new FavoriteEntity();
		favorite.user = user;
		favorite.bookItem = bookItem;
		favorite.createdAt = LocalDateTime.now();
		return favorite;
	}
}