package com.corp.bookiki.bookitem.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;
import com.corp.bookiki.qrcode.entity.QrCodeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "book_items")
@Getter
public class BookItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_information_id")
	private BookInformationEntity bookInformation;

	@Column(name = "purchase_at")
	private LocalDateTime purchaseAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_type")
	private BookStatus bookStatus = BookStatus.AVAILABLE;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;


	@OneToOne(mappedBy = "bookItem")
	private QrCodeEntity qrCode;

	@Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
	private Boolean deleted = false;

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	@OneToMany(mappedBy = "bookItem")
	private List<BookHistoryEntity> bookHistories = new ArrayList<>();

	public boolean isAvailable() {
		return bookStatus == BookStatus.AVAILABLE;
	}

	public void borrow() {
		if (!isAvailable()) {
			throw new BookItemException(ErrorCode.BOOK_ALREADY_BORROWED);
		}
		this.bookStatus = BookStatus.BORROWED;
	}
}
