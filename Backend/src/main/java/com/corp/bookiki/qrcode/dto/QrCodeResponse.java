package com.corp.bookiki.qrcode.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.qrcode.entity.QrCodeEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "QR 코드 응답 정보")
public class QrCodeResponse {
	@Schema(
		description = "QR 코드 고유 식별자",
		example = "1",
		required = true
	)
	private Integer id;

	@Schema(
		description = "QR 코드 URL",
		example = "https://naver.com/123",
		required = true
	)
	private String qrValue;

	@Schema(
		description = "QR 코드 생성 시간",
		example = "2024-02-03T10:30:00",
		required = true
	)
	private LocalDateTime createdAt;

	public QrCodeResponse(QrCodeEntity qrCode) {
		this.id = qrCode.getId();
		this.qrValue = qrCode.getQrValue();
		this.createdAt = qrCode.getCreatedAt();
	}
}