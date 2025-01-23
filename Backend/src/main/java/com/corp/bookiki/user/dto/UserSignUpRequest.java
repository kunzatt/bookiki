package com.corp.bookiki.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class UserSignUpRequest {
	@Schema(
		description = "이메일",
		example = "user@example.com",
		required = true
	)
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	private String email;

	@Schema(
		description = "비밀번호",
		example = "Password123!",
		required = true,
		minLength = 8,
		maxLength = 20
	)
	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
		message = "비밀번호는 8~20자의 영문, 숫자, 특수문자를 포함해야 합니다"
	)
	private String password;

	@Schema(
		description = "사용자 이름",
		example = "홍길동",
		required = true,
		minLength = 2,
		maxLength = 50
	)
	@NotBlank(message = "이름은 필수 입력값입니다.")
	@Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
	private String userName;

	@Schema(
		description = "사번",
		example = "CORP001",
		required = true
	)
	@NotBlank(message = "사번은 필수 입력값입니다.")
	private String companyId;
}