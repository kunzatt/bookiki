package com.corp.bookiki.iot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private final IotWebSocketHandler webSocketHandler;
	private final ObjectMapper objectMapper;

	// 책 위치 LED 표시 요청
	@Transactional(readOnly = true)
	public void requestBookLocation(String bookId, Integer shelfId) {
		try {
			ShelfEntity shelf = shelfRepository.findById(shelfId)
				.orElseThrow(() -> new IllegalArgumentException("Shelf not found"));

			IotMessage message = IotMessage.bookLocation(bookId, shelf);
			webSocketHandler.sendToAll(objectMapper.writeValueAsString(message));
		} catch (Exception e) {
			log.error("Error requesting book location: {}", e.getMessage());
			throw new RuntimeException("Failed to request book location", e);
		}
	}

	// 책 반납 처리
	@Transactional
	public void handleBookReturn(String bookId) {
		try {
			// 여기에 책 반납 처리 로직 구현
			log.info("Book return processed: {}", bookId);
		} catch (Exception e) {
			log.error("Error processing book return: {}", e.getMessage());
			throw new RuntimeException("Failed to process book return", e);
		}
	}

	// 책 위치 업데이트
	@Transactional
	public void updateBookLocation(String bookId, Integer shelfId) {
		try {
			ShelfEntity shelf = shelfRepository.findById(shelfId)
				.orElseThrow(() -> new IllegalArgumentException("Shelf not found"));

			// 여기에 책 위치 업데이트 로직 구현
			log.info("Book location updated: {} to shelf: {}", bookId, shelfId);
		} catch (Exception e) {
			log.error("Error updating book location: {}", e.getMessage());
			throw new RuntimeException("Failed to update book location", e);
		}
	}
}
