package com.corp.bookiki.iot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.iot.dto.IotMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "WebSocket", description = "WebSocket 연결 및 메시지 전송 API")
public class WebSocketController {

	@Operation(summary = "WebSocket 연결 엔드포인트")
	@GetMapping("/iot/ws")
	public void connect() {
		// WebSocket 연결용 엔드포인트
	}

	@Operation(summary = "책 반납 메시지 전송")
	@PostMapping("/api/ws/book-return")
	public void sendBookReturn(@RequestBody IotMessage message) {
		// 테스트용 REST 엔드포인트
	}

	@Operation(summary = "위치 업데이트 메시지 전송")
	@PostMapping("/api/ws/location-update")
	public void sendLocationUpdate(@RequestBody IotMessage message) {
		// 테스트용 REST 엔드포인트
	}
}
