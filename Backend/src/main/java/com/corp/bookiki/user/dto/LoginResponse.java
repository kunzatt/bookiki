package com.corp.bookiki.user.dto;

import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.security.CustomUserDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private Integer id;
    private String username;
    private Role role;

    public static LoginResponse from(CustomUserDetails securityUser) {
        UserEntity user = securityUser.getUser();
        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUserName())
                .role(user.getRole())
                .build();
    }
}
