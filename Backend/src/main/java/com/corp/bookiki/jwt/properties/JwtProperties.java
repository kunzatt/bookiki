package com.corp.bookiki.jwt.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Component
@Validated  //유효성 검증 활성화
@ConfigurationProperties(prefix = "jwt")    //application.yml/properties의 jwt 접두어로 시작하는 설정을 바인딩
public class JwtProperties {

    @NotBlank(message = "JWT secret key cannot be empty")
    private String secret;

    @NotNull(message = "Access token expiration cannot be null")
    @Positive(message = "Access token expiration must be positive")
    private Long accessTokenExpiration;

    @NotNull(message = "Refresh token expiration cannot be null")
    @Positive(message = "Refresh token expiration must be positive")
    private Long refreshTokenExpiration;

    @NotNull(message = "Temporary token expiration cannot be null")
    @Positive(message = "Temporary token expiration must be positive")
    private Long temporaryTokenExpiration;

    @NotBlank(message = "JWT issuer cannot be empty")
    private String issuer;

    @NotBlank(message = "JWT subject prefix cannot be empty")
    private String subjectPrefix;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setAccessTokenExpiration(Long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public void setTemporaryTokenExpiration(Long temporaryTokenExpiration) {
        this.temporaryTokenExpiration = temporaryTokenExpiration;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }
}
