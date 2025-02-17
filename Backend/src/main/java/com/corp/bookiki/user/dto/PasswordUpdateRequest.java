package com.corp.bookiki.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "비밀번호 변경 요청")
public class PasswordUpdateRequest {

	@Schema(
		description = "현재 비밀번호",
		example = "currentPassword123!",
		required = true
	)
	@NotBlank(message = "현재 비밀번호는 필수입니다.")
	private String currentPassword;

	@Schema(
		description = "새 비밀번호",
		example = "newPassword123!",
		required = true
	)
	@NotBlank(message = "새 비밀번호는 필수입니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
		message = "비밀번호는 8~20자의 영문, 숫자, 특수문자를 포함해야 합니다"
	)
	private String newPassword;

	@Schema(
		description = "새 비밀번호 확인",
		example = "newPassword123!",
		required = true
	)
	@NotBlank(message = "새 비밀번호 확인은 필수입니다.")
	private String newPasswordConfirm;
}
