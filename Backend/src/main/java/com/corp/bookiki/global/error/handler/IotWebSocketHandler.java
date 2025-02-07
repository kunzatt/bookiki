package com.corp.bookiki.global.error.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IotWebSocketHandler extends TextWebSocketHandler {

	private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String sessionId = session.getId();
		sessions.put(sessionId, session);
		log.info("IoT 디바이스 연결됨: {}", sessionId);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		log.info("메시지 수신: {}", payload);

		// JSON 메시지 파싱
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(payload);
		String type = jsonNode.get("type").asText();

		switch (type) {
			case "BOOK_RETURN":
				handleBookReturn(jsonNode);
				break;
			case "LOCATION_UPDATE":
				handleLocationUpdate(jsonNode);
				break;
			default:
				log.warn("알 수 없는 메시지 타입: {}", type);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(session.getId());
		log.info("IoT 디바이스 연결 해제: {}", session.getId());
	}

	// IoT 디바이스로 LED 위치 정보 전송
	public void sendBookLocation(String bookId, String location) throws IOException {
		Map<String, Object> message = new HashMap<>();
		message.put("type", "BOOK_LOCATION");
		message.put("bookId", bookId);
		message.put("location", location);

		sendToAll(new ObjectMapper().writeValueAsString(message));
	}

	public void handleBookReturn(JsonNode jsonNode) {
		// QR 코드 스캔 후 반납 처리 로직
		String bookId = jsonNode.get("bookId").asText();
		// 반납 처리 서비스 호출
	}

	public void handleLocationUpdate(JsonNode jsonNode) {
		// 책 위치 변경 처리 로직
		String bookId = jsonNode.get("bookId").asText();
		String newLocation = jsonNode.get("location").asText();
		// 위치 업데이트 서비스 호출
	}

	public void sendToAll(String message) throws IOException {
		for (WebSocketSession session : sessions.values()) {
			if (session.isOpen()) {
				session.sendMessage(new TextMessage(message));
			}
		}
	}
}