package com.corp.bookiki.user.dto;

import com.corp.bookiki.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import com.corp.bookiki.user.entity.Role;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    private Integer id;

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private String userName;

    @NotBlank(message = "사번은 필수 입력값입니다.")
    private String companyId;

    private Role role;


    public static AuthUser from(UserEntity user) {
        return AuthUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .companyId(user.getCompanyId())
                .role(user.getRole())
                .build();
    }
}
