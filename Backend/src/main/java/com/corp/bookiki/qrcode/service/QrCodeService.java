package com.corp.bookiki.qrcode.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.qrcode.dto.QrCodeResponseDto;
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

	public QrCodeResponseDto createQRCode(Integer bookItemId) {
		BookItemEntity bookItem = bookItemRepository.findById(bookItemId)
			.orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다."));

		if (qrCodeRepository.findByBookItemId(bookItemId).isPresent()) {
			throw new IllegalStateException("이미 QR 코드가 존재합니다.");
		}

		QrCodeEntity qrCode = QrCodeEntity.create(bookItem);
		return new QrCodeResponseDto(qrCodeRepository.save(qrCode));
	}

}