package com.corp.bookiki.iot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.iot.dto.IotMessage;
import com.corp.bookiki.iot.service.IotService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "IoT 연결", description = "IoT와 WebSocket 연결 및 메시지 전송 API")
public class IotController {

	private final IotService iotService;


	@Operation(summary = "책 위치 LED 표시 요청")
	@GetMapping("/api/ws/book-location/{bookItemId}")
	public ResponseEntity<IotMessage> requestBookLocation(
		@Parameter(description = "책 ID", required = true)
		@PathVariable Integer bookItemId) {
		// 반환 타입을 Integer에서 IotMessage로 변경
		return ResponseEntity.ok(iotService.requestBookLocation(bookItemId));
	}

	@Operation(summary = "책 반납 메시지 테스트")
	@PostMapping("/api/ws/book-return")
	public ResponseEntity<IotMessage> sendBookReturn(
		@RequestBody IotMessage message) {
		return ResponseEntity.ok(iotService.handleBookReturn(message.getBookId()));
	}

	@Operation(summary = "위치 업데이트 메시지 테스트")
	@PostMapping("/api/ws/location-update")
	public ResponseEntity<IotMessage> sendLocationUpdate(
		@RequestBody IotMessage message) {
		return ResponseEntity.ok(
			iotService.updateBookLocation(message.getBookId(), message.getShelf().getId())
		);
	}
}