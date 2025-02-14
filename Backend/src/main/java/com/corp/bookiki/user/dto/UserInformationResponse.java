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

	@Schema(description = "프로필 이미지 경로")
	private String profileImage;

	@Schema(description = "대출 가능 여부", example = "현재 3권 대출 가능")
	private String loanStatus;

	@Builder
	public UserInformationResponse(String userName, String companyId,
		String profileImage, String loanStatus) {
		this.userName = userName;
		this.companyId = companyId;
		this.profileImage = profileImage;
		this.loanStatus = loanStatus;
	}

	public static UserInformationResponse from(UserEntity user, Integer availableLoans) {
		String status = availableLoans > 0
			? String.format("현재 %d권 대출 가능", availableLoans)
			: "대출 불가능";

		return UserInformationResponse.builder()
			.userName(user.getUserName())
			.companyId(user.getCompanyId())
			.profileImage(user.getProfileImage())
			.loanStatus(status)
			.build();
	}
}
