package com.corp.bookiki.qrcode.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.corp.bookiki.bookitem.entity.BookItemEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qr_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QrCodeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "qr_value")
	private String qrValue;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_item_id")
	private BookItemEntity bookItem;

	@Builder
	private QrCodeEntity(BookItemEntity bookItem) {
		this.qrValue = UUID.randomUUID().toString();
		this.createdAt = LocalDateTime.now();
		this.bookItem = bookItem;
	}
}
