package com.corp.bookiki.iot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.handler.IotWebSocketHandler;
import com.corp.bookiki.iot.dto.IotMessage;
import com.corp.bookiki.shelf.entity.ShelfEntity;
import com.corp.bookiki.shelf.repository.ShelfRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IotService {

	private final ShelfRepository shelfRepository;
	private final BookItemRepository bookItemRepository;
	private final IotWebSocketHandler webSocketHandler;
	private final ObjectMapper objectMapper;

	// 책 위치 LED 표시 요청
	@Transactional(readOnly = true)
	public IotMessage requestBookLocation(Integer bookItemId) {
		try {
			BookItemEntity bookItem = bookItemRepository.findById(bookItemId)
				.orElseThrow(() -> new IllegalArgumentException("BookItem not found"));

			Integer category = bookItem.getBookInformation().getCategory();

			ShelfEntity shelf = shelfRepository.findByCategory(category)
				.orElseThrow(() -> new IllegalArgumentException("Shelf not found for category: " + category));

			IotMessage message = IotMessage.bookLocation(bookItemId.toString(), shelf);
			webSocketHandler.sendToAll(objectMapper.writeValueAsString(message));

			return message;  // IotMessage 반환
		} catch (Exception e) {
			log.error("LED 위치 표시 요청 실패: {}", e.getMessage());
			throw new RuntimeException("Failed to request book location", e);
		}
	}


	// 책 반납 처리
	@Transactional
	public IotMessage handleBookReturn(String bookId) {
		try {
			IotMessage message = IotMessage.bookReturn(bookId);
			webSocketHandler.sendToAll(objectMapper.writeValueAsString(message));
			return message;
		} catch (Exception e) {
			log.error("책 반납 처리 실패: {}", e.getMessage());
			throw new RuntimeException("Failed to process book return", e);
		}
	}

	// 책 위치 업데이트
	@Transactional
	public IotMessage updateBookLocation(String bookId, Integer shelfId) {
		try {
			ShelfEntity shelf = shelfRepository.findById(shelfId)
				.orElseThrow(() -> new IllegalArgumentException("Shelf not found"));

			IotMessage message = IotMessage.locationUpdate(bookId, shelf);
			webSocketHandler.sendToAll(objectMapper.writeValueAsString(message));
			return message;
		} catch (Exception e) {
			log.error("책 위치 업데이트 실패: {}", e.getMessage());
			throw new RuntimeException("Failed to update book location", e);
		}
	}
}