package com.corp.bookiki.qrcode.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.qrcode.dto.QrCodeResponse;
import com.corp.bookiki.qrcode.service.QrCodeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "QR 코드 API", description = "도서 QR 코드 생성 및 조회 API")
public class QrCodeController {

	private final QrCodeService qrCodeService;

	@Operation(summary = "QR 코드 생성", description = "도서 ID를 이용하여 새로운 QR 코드를 생성합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "QR 코드 생성 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = QrCodeResponse.class),
				examples = @ExampleObject(value = """
                    {
                        "id": 1,
                        "qrValue": "https://bookiki.com/books/123",
                        "createdAt": "2024-02-03T10:30:00"
                    }
                """)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "존재하지 않는 도서",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					name = "도서 미존재",
					value = """
                    {
                        "timestamp": "2024-02-03T10:30:00",
                        "status": 400,
                        "message": "도서를 찾을 수 없습니다",
                        "errors": []
                    }
                    """
				)
			)
		)
	})
	@PostMapping("/api/admin/qrcodes/{bookItemId}")
	public ResponseEntity<QrCodeResponse> createQRCode(
		@Parameter(
			description = "도서 아이템 ID",
			required = true,
			example = "123"
		)
		@PathVariable Integer bookItemId
	) {
		log.info("QR 코드 생성 요청: 도서 아이템 ID {}", bookItemId);
		return ResponseEntity.ok(qrCodeService.createQRCode(bookItemId));
	}

}