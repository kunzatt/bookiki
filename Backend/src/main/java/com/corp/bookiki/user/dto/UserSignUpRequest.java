package com.corp.bookiki.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSignUpRequest {
    // 이메일 (필수, 이메일 형식 검증)
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 비밀번호 (필수)
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    // 사용자 이름 (필수)
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String userName;

    // 사번 (필수)
    @NotBlank(message = "사번은 필수 입력값입니다.")
    private String companyId;
}