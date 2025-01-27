package com.corp.bookiki.global.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// Common
	INVALID_INPUT_VALUE(400, "잘못된 입력값입니다"),
	INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다"),
	UNAUTHORIZED(401, "인증되지 않은 접근입니다"),
	FORBIDDEN(403, "권한이 없습니다"),
	INVALID_ENUM_VALUE(400, "잘못된 상태값입니다"),

	// Auth & User
	INVALID_TOKEN(401, "유효하지 않은 토큰입니다"),
	EXPIRED_TOKEN(401, "만료된 토큰입니다"),
	REFRESH_TOKEN_NOT_FOUND(401, "리프레시 토큰을 찾을 수 없습니다."),
	EMAIL_DUPLICATE(400, "이미 존재하는 이메일입니다"),
	COMPANY_ID_DUPLICATE(400, "이미 존재하는 사번입니다"),
	USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다"),
	INVALID_PASSWORD(400, "잘못된 비밀번호입니다"),
	INVALID_ROLE_TYPE(400, "잘못된 권한 타입입니다"),
	INVALID_PROFILE_IMAGE(400, "잘못된 프로필 이미지 형식입니다"),

	// Book
	BOOK_INFO_NOT_FOUND(404, "도서 정보를 찾을 수 없습니다"),
	BOOK_ITEM_NOT_FOUND(404, "도서 아이템을 찾을 수 없습니다"),
	INVALID_ISBN(400, "잘못된 ISBN 입니다"),
	INVALID_BOOK_STATUS(400, "잘못된 도서 상태입니다"),
	BOOK_ALREADY_BORROWED(400, "이미 대출 중인 도서입니다"),
	BOOK_NOT_AVAILABLE(400, "현재 대출 불가능한 도서입니다"),
	BOOK_ALREADY_DELETED(400, "이미 삭제된 도서입니다"),
	DUPLICATE_BOOK_LOCATION(400, "이미 위치가 지정된 도서입니다"),

	// Shelf & Location
	SHELF_NOT_FOUND(404, "책장을 찾을 수 없습니다"),
	INVALID_SHELF_NUMBER(400, "잘못된 책장 번호입니다"),
	INVALID_LINE_NUMBER(400, "잘못된 행 번호입니다"),
	LOCATION_NOT_FOUND(404, "도서 위치를 찾을 수 없습니다"),

	// QR
	QR_NOT_FOUND(404, "QR 코드를 찾을 수 없습니다"),
	INVALID_QR_VALUE(400, "유효하지 않은 QR 코드입니다"),
	DUPLICATE_QR_CODE(400, "이미 등록된 QR 코드입니다"),

	// History & Favorite
	HISTORY_NOT_FOUND(404, "대출 이력을 찾을 수 없습니다"),
	INVALID_RETURN_DATE(400, "잘못된 반납 날짜입니다"),
	DUPLICATE_FAVORITE(400, "이미 즐겨찾기에 추가된 도서입니다"),
	FAVORITE_NOT_FOUND(404, "즐겨찾기를 찾을 수 없습니다"),

	// Notification
	NOTIFICATION_NOT_FOUND(404, "알림을 찾을 수 없습니다"),
	INVALID_NOTIFICATION_TYPE(400, "잘못된 알림 유형입니다"),
	INVALID_NOTIFICATION_STATUS(400, "잘못된 알림 상태입니다"),

	// Notice & QnA
	NOTICE_NOT_FOUND(404, "공지사항을 찾을 수 없습니다"),
	QNA_NOT_FOUND(404, "QnA를 찾을 수 없습니다"),
	COMMENT_NOT_FOUND(404, "댓글을 찾을 수 없습니다"),
	INVALID_PARENT_COMMENT(400, "잘못된 부모 댓글입니다"),

	// Chat
	CHAT_NOT_FOUND(404, "채팅을 찾을 수 없습니다"),
	INVALID_CHAT_CONTENT(400, "잘못된 채팅 내용입니다"),

	// 외부 API
	EXTERNAL_API_ERROR(500, "외부 API 호출 중 오류가 발생했습니다");

	private final int status;
	private final String message;
}
