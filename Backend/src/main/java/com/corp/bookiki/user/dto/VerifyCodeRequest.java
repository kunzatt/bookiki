package com.corp.bookiki.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이메일 인증 코드 확인 요청")
public class VerifyCodeRequest {

	@Schema(
		description = "인증 코드",
		example = "123456",
		required = true,
		pattern = "^[0-9]{6}$"
	)
	@NotBlank(message = "인증코드는 필수입니다.")
	@Pattern(regexp = "^[0-9]{6}$", message = "인증코드는 6자리 숫자여야 합니다.")
	private String code;
}