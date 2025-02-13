package com.corp.bookiki.iot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.iot.service.IotStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/iot-storage")
@RequiredArgsConstructor
@Tag(name = "이미지 관리", description = "이미지 문자열 저장 및 조회 API")
public class IotStorageController {

	private final IotStorageService iotStorageService;

	@Operation(summary = "이미지 문자열 저장",
		description = "이미지 문자열을 서버에 저장합니다. 새로운 이미지가 저장되면 기존 이미지는 덮어쓰기됩니다.")
	@ApiResponse(responseCode = "200", description = "이미지 저장 성공")
	@PostMapping("")
	public ResponseEntity<Void> saveImage(
		@Parameter(description = "이미지 문자열", required = true)
		@RequestBody String imageString) {
		iotStorageService.saveImageString(imageString);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "이미지 문자열 조회",
		description = "서버에 저장된 이미지 문자열을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "이미지 조회 성공")
	@GetMapping("")
	public ResponseEntity<String> getImage() {
		String imageString = iotStorageService.getImageString();
		return ResponseEntity.ok(imageString);
	}
}