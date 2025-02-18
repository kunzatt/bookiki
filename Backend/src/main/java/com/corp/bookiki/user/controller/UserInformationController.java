package com.corp.bookiki.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.dto.UserInformationResponse;
import com.corp.bookiki.user.service.UserInformationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "현재 사용자 관련 API")
@Slf4j
public class UserInformationController {

	private final UserInformationService userInformationService;

	@Operation(summary = "현재 사용자 조회", description = "현재 사용자의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = UserInformationResponse.class)
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
	public ResponseEntity<UserInformationResponse> getUserInformation(
		@CurrentUser AuthUser authUser
	) {
		log.info("현재 사용자 정보 조회");
		return ResponseEntity.ok(userInformationService.getUserInformation(authUser.getId()));
	}
}