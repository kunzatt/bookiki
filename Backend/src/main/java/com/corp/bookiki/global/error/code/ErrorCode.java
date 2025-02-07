package com.corp.bookiki.global.error.code;

import org.springframework.http.HttpStatus;

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
	INVALID_PASSWORD(400, "잘못된 비밀번호입니다"),
	INVALID_ROLE_TYPE(400, "잘못된 권한 타입입니다"),
	INVALID_PROFILE_IMAGE(400, "잘못된 프로필 이미지 형식입니다"),
	LOGIN_BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED.value(), "이메일 또는 비밀번호가 일치하지 않습니다."),
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED.value(), "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요."),
	USER_NOT_ACTIVE(400, "대출 가능 기한이 아니므로 대출이 불가능합니다"),
	// 사용자 관련 에러
	USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "사용자를 찾을 수 없습니다."),
	USER_SEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "사용자 정보 조회 중 오류가 발생했습니다."),
	INVALID_EMAIL_VERIFICATION(401, "유효하지 않은 인증 토큰입니다"),
	EXPIRED_EMAIL_VERIFICATION(401, "만료된 인증 토큰입니다"),
	DUPLICATE_EMAIL(400, "이미 등록된 이메일입니다"),
	FAIL_EMAIL_SEND(500, "이메일 발송에 실패했습니다"),
	HAS_OVERDUE_BOOKS(400, "이미 연체된 도서가 있습니다"),
	BORROW_LIMIT_EXCEEDED(400, "대출 한도가 초과했습니다."),

	// OAuth2 관련 에러 코드들
	OAUTH2_INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 OAuth2 토큰입니다."),
	OAUTH2_INVALID_REQUEST(HttpStatus.BAD_REQUEST.value(), "잘못된 OAuth2 요청입니다."),
	OAUTH2_UNAUTHORIZED_CLIENT(HttpStatus.UNAUTHORIZED.value(), "승인되지 않은 OAuth2 클라이언트입니다."),
	OAUTH2_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "OAuth2 접근이 거부되었습니다."),
	OAUTH2_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "OAuth2 처리 중 오류가 발생했습니다."),

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
	INVALID_INPUT_VALUE_NO_DATE(400, "사용자 정의 기간 조회 시 시작일과 종료일은 필수입니다"),
	INVALID_INPUT_VALUE_AFTER_DATE(400, "시작일이 종료일보다 늦을 수 없습니다"),

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
	EXTERNAL_API_ERROR(500, "외부 API 호출 중 오류가 발생했습니다"),

	// Loan Policy
	LOAN_POLICY_NOT_FOUND(404, "대출 정책을 찾을 수 없습니다"),
	INVALID_LOAN_POLICY(400, "잘못된 대출 정책입니다"),
	INVALID_MAX_BOOKS(400, "올바르지 않은 최대 대출 가능 권수입니다."),
	INVALID_LOAN_PERIOD(400, "올바르지 않은 대출 기간입니다."),

	// 챗봇 관련
	CHATBOT_NOT_AVAILABLE(500, "챗봇 서비스를 일시적으로 사용할 수 없습니다"),
	INVALID_INTENT(400, "올바르지 않은 의도 형식입니다"),
	SESSION_NOT_FOUND(404, "대화 세션을 찾을 수 없습니다"),
	CONTEXT_NOT_FOUND(404, "대화 컨텍스트를 찾을 수 없습니다"),
	INVALID_WEBHOOK_REQUEST(400, "올바르지 않은 웹훅 요청입니다"),
	DIALOGFLOW_API_ERROR(500, "Dialogflow API 오류가 발생했습니다"),
	INTENT_PROCESSING_ERROR(500, "의도 처리 중 오류가 발생했습니다"),
	CHATBOT_SESSION_EXPIRED(401, "대화 세션이 만료되었습니다"),
	DIALOGFLOW_CLIENT_ERROR(500, "Dialogflow 클라이언트 생성에 실패했습니다"),
	CHATBOT_CONTEXT_INVALID(400, "잘못된 대화 컨텍스트입니다"),
	DIALOGFLOW_PROCESSING_ERROR(400, "챗봇 메시지 처리 중 오류가 발생했습니다."),
	ENTITY_EXTRACTION_ERROR(400, "엔티티 추출 중 오류가 발생했습니다."),
	CONTEXT_MANAGEMENT_ERROR(400, "컨텍스트 관리 중 오류가 발생했습니다."),

	// 대화 이력 관련
	CONVERSATION_HISTORY_ERROR(500, "대화 이력 처리 중 오류가 발생했습니다"),
	PREFERENCE_UPDATE_ERROR(500, "사용자 선호도 업데이트 중 오류가 발생했습니다"),
	KNOWLEDGE_BASE_ERROR(500, "지식 베이스 처리 중 오류가 발생했습니다"),

	// 개인화 관련
	PERSONALIZATION_ERROR(500, "응답 개인화 중 오류가 발생했습니다"),
	USER_PREFERENCE_NOT_FOUND(404, "사용자 선호도 정보를 찾을 수 없습니다");

	private final int status;
	private final String message;
}
