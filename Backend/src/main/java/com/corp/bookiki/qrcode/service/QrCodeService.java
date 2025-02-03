package com.corp.bookiki.qrcode.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.qrcode.entity.QrCodeEntity;
import com.corp.bookiki.qrcode.repository.QrCodeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class QrCodeService {

	private final QrCodeRepository qrCodeRepository;
	private final BookItemRepository bookItemRepository;

	public QRCodeResponse createQRCode(Long bookItemId) {
		BookItemEntity bookItem = bookItemRepository.findById(bookItemId)
			.orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다."));

		if (qrCodeRepository.findByBookItemId(bookItemId).isPresent()) {
			throw new IllegalStateException("이미 QR 코드가 존재합니다.");
		}

		QrCodeEntity qrCode = QrCodeEntity.createBuilder()
			.bookItem(bookItem)
			.build();

		return new QRCodeResponse(qrCodeRepository.save(qrCode));
	}

	// QR 코드로 도서 정보 조회
	public BookItemResponse getBookItemByQRCode(String qrValue) {
		BookItem bookItem = bookItemRepository.findByQrCodeQrValue(qrValue)
			.orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다."));

		return new BookItemResponse(bookItem);
	}

}
