package com.corp.bookiki.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OAuth2SignUpRequest {
    @NotBlank(message = "사번은 필수 입력값입니다")
    private String companyId;

    @NotBlank(message = "이름은 필수 입력값입니다")
    private String userName;
}
