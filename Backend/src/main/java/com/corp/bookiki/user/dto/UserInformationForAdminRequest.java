package com.corp.bookiki.user.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사용자 활성 시간 수정 요청")
public class UserInformationForAdminRequest {

	@Schema(description = "활성 시간", example = "2024-01-31T12:00:00")
	private LocalDateTime activeAt;
}
