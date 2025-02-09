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
	private final ObjectMapper objectMapper = new ObjectMapper();  // 객체 재사용을 위해 필드로 이동

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String sessionId = session.getId();
		sessions.put(sessionId, session);
		log.info("IoT 디바이스 연결됨 - Session ID: {}, 현재 연결 수: {}", sessionId, sessions.size());

		// 테스트용 웰컴 메시지 전송
		try {
			Map<String, String> welcomeMsg = new HashMap<>();
			welcomeMsg.put("type", "CONNECT_SUCCESS");
			welcomeMsg.put("message", "WebSocket 연결이 성공적으로 완료되었습니다.");
			session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMsg)));
		} catch (IOException e) {
			log.error("웰컴 메시지 전송 실패", e);
		}
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		log.info("메시지 수신 - Session ID: {}, Payload: {}", session.getId(), payload);

		try {
			JsonNode jsonNode = objectMapper.readTree(payload);
			String type = jsonNode.get("type").asText();

			log.info("메시지 타입: {}", type);

			switch (type) {
				case "BOOK_RETURN":
					handleBookReturn(jsonNode);
					// 테스트용 응답
					sendTestResponse(session, "BOOK_RETURN_RECEIVED", "도서 반납 메시지를 수신했습니다.");
					break;
				case "LOCATION_UPDATE":
					handleLocationUpdate(jsonNode);
					// 테스트용 응답
					sendTestResponse(session, "LOCATION_UPDATE_RECEIVED", "위치 업데이트 메시지를 수신했습니다.");
					break;
				case "TEST_MESSAGE":  // 테스트용 메시지 타입 추가
					log.info("테스트 메시지 수신: {}", jsonNode.get("message").asText());
					sendTestResponse(session, "TEST_RESPONSE", "테스트 메시지가 정상적으로 수신되었습니다.");
					break;
				default:
					log.warn("알 수 없는 메시지 타입: {}", type);
					sendTestResponse(session, "ERROR", "알 수 없는 메시지 타입입니다.");
			}
		} catch (Exception e) {
			log.error("메시지 처리 중 오류 발생", e);
			sendTestResponse(session, "ERROR", "메시지 처리 중 오류가 발생했습니다.");
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String sessionId = session.getId();
		sessions.remove(sessionId);
		log.info("IoT 디바이스 연결 해제 - Session ID: {}, Status: {}, 남은 연결 수: {}",
			sessionId, status.getReason(), sessions.size());
	}

	// IoT 디바이스로 LED 위치 정보 전송
	public void sendBookLocation(String bookId, String location) throws IOException {
		Map<String, Object> message = new HashMap<>();
		message.put("type", "BOOK_LOCATION");
		message.put("bookId", bookId);
		message.put("location", location);

		String messageStr = objectMapper.writeValueAsString(message);
		log.info("발신할 도서 위치 메시지: {}", messageStr);
		sendToAll(messageStr);
	}

	private void handleBookReturn(JsonNode jsonNode) {
		String bookId = jsonNode.get("bookId").asText();
		log.info("도서 반납 처리 - Book ID: {}", bookId);
		// 반납 처리 서비스 호출
	}

	private void handleLocationUpdate(JsonNode jsonNode) {
		String bookId = jsonNode.get("bookId").asText();
		String newLocation = jsonNode.get("location").asText();
		log.info("위치 업데이트 처리 - Book ID: {}, New Location: {}", bookId, newLocation);
		// 위치 업데이트 서비스 호출
	}

	private void sendTestResponse(WebSocketSession session, String type, String message) {
		try {
			Map<String, String> response = new HashMap<>();
			response.put("type", type);
			response.put("message", message);
			session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
		} catch (IOException e) {
			log.error("테스트 응답 전송 실패", e);
		}
	}

	public void sendToAll(String message) throws IOException {
		log.info("전체 발송 시도 - 현재 연결된 세션 수: {}", sessions.size());
		for (WebSocketSession session : sessions.values()) {
			if (session.isOpen()) {
				session.sendMessage(new TextMessage(message));
				log.info("메시지 전송 완료 - Session ID: {}", session.getId());
			} else {
				log.warn("세션이 닫혀있음 - Session ID: {}", session.getId());
			}
		}
	}
}