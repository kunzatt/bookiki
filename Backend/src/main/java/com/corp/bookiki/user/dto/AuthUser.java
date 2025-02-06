package com.corp.bookiki.user.dto;

import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    private Integer id;
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    private String name;
    private Role role;

    public static AuthUser from(UserEntity user) {
        return AuthUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUserName())
                .role(user.getRole())
                .build();
    }
}
