package com.corp.bookiki.qrcode.entity;

import java.time.LocalDateTime;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qr_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QrCodeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "qr_value")
	private String qrValue;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_item_id")
	private BookItemEntity bookItem;

	public static QrCodeEntity create(BookItemEntity bookItem) {
		QrCodeEntity qrCode = new QrCodeEntity();
		qrCode.qrValue = "http://i12a206.p.ssafy.io/qr/books/" + bookItem.getId();
		qrCode.createdAt = LocalDateTime.now();
		qrCode.bookItem = bookItem;
		return qrCode;
	}
}