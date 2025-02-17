package com.corp.bookiki.iot.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.global.error.handler.IotWebSocketHandler;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class IotTestController {

	private final IotWebSocketHandler webSocketHandler;  // 주입 받아서 사용

	@PostMapping("/api/ws/test/send")
	public ResponseEntity<String> sendTestMessage(@RequestBody String message) {
		try {
			webSocketHandler.sendToAll(message);
			return ResponseEntity.ok("메시지 전송 성공");
		} catch (IOException e) {
			return ResponseEntity.internalServerError().body("메시지 전송 실패");
		}
	}
}
