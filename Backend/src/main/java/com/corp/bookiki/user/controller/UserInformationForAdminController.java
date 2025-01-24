package com.corp.bookiki.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.user.dto.UserInformationForAdminResponse;
import com.corp.bookiki.user.service.UserInformationForAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "관리자 API", description = "관리자용 사용자 관리 API")
@Slf4j
public class UserInformationForAdminController {

	private final UserInformationForAdminService userInformationForAdminService;

	@Operation(summary = "전체 사용자 조회", description = "모든 사용자의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = UserInformationForAdminResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "사용자가 존재하지 않음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@GetMapping
	public ResponseEntity<List<UserInformationForAdminResponse>> getUserDetails() {
		log.info("전체 사용자 정보 조회");
		return ResponseEntity.ok(userInformationForAdminService.getUserDetails());
	}

	@Operation(summary = "개별 사용자 조회", description = "특정 ID를 가진 사용자의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = UserInformationForAdminResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "사용자가 존재하지 않음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@GetMapping("/{userId}")
	public ResponseEntity<UserInformationForAdminResponse> getUserDetailsById(@PathVariable Integer userId) {
		log.info("사용자 ID {} 상세 정보 조회", userId);
		return ResponseEntity.ok(userInformationForAdminService.getUserDetailsById(userId));
	}
}