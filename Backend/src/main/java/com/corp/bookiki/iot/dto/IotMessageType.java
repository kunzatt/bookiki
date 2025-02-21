package com.corp.bookiki.iot.dto;

public enum IotMessageType {
	BOOK_RETURN,      // IoT -> 서버: 책 반납
	LOCATION_UPDATE,  // IoT -> 서버: 위치 변경
	BOOK_LOCATION    // 서버 -> IoT: LED 위치 표시
}
