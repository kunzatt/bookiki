package com.corp.bookiki.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "테스트 계정 생성 요청")
public class TestAccountRequest {

    @Schema(description = "이메일", example = "test@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "비밀번호", example = "test123")
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @Schema(description = "사용자 이름", example = "테스트")
    @NotBlank(message = "이름은 필수입니다")
    private String userName;

    @Schema(description = "사번", example = "TEST001")
    @NotBlank(message = "사번은 필수입니다")
    private String companyId;
}
