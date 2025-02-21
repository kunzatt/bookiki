package com.corp.bookiki.user.dto;

import com.corp.bookiki.user.entity.UserEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "사용자 간단 정보 응답")
public class UserInformationResponse {

	@Schema(description = "사용자 이름", example = "홍길동")
	private String userName;

	@Schema(description = "사번", example = "CORP001")
	private String companyId;

	@Schema(description = "이메일", example = "user@example.com")
	private String email;

	@Schema(description = "대출 가능 여부", example = "현재 3권 대출 가능")
	private String loanStatus;

	@Schema(description = "로그인 provider", example = "BOOKIKI")
	private String provider;

	@Builder
	public UserInformationResponse(String userName, String companyId, String email, String loanStatus, String provider) {
		this.userName = userName;
		this.companyId = companyId;
		this.email = email;
		this.loanStatus = loanStatus;
		this.provider = provider;
	}

	public static UserInformationResponse from(UserEntity user, Integer availableLoans) {
		String status = availableLoans > 0
			? String.format("현재 %d권 대출 가능", availableLoans)
			: "대출 불가능";

		return UserInformationResponse.builder()
			.userName(user.getUserName())
			.companyId(user.getCompanyId())
			.email(user.getEmail())
			.loanStatus(status)
			.provider(user.getProvider().name())
			.build();
	}
}
