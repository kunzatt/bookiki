package com.corp.bookiki.user.dto;

import com.corp.bookiki.user.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "관리자용 사용자 정보 응답")
public class UserInformationForAdminResponse {
	@Schema(description = "사용자 ID")
	private Integer id;

	@Schema(description = "이메일", example = "user@example.com")
	private String email;

	@Schema(description = "사용자 이름", example = "홍길동")
	private String userName;

	@Schema(description = "사번", example = "CORP001")
	private String companyId;

	@Schema(description = "사용자 역할", example = "USER")
	private String role;

	@Schema(description = "계정 생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "정보 수정일시")
	private LocalDateTime updatedAt;

	@Schema(description = "최종 활동일시")
	private LocalDateTime activeAt;

	@Schema(description = "프로필 이미지 경로")
	private String profileImage;

	@Schema(description = "로그인 도메인")
	private String provider;

	private int currentBorrowCount;  // 현재 대출 중인 도서 수
	private boolean hasOverdueBooks; // 연체 도서 존재 여부

	@Builder
	public UserInformationForAdminResponse(Integer id, String email, String userName, String companyId,
		String role, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime activeAt,
		String profileImage, String provider, int currentBorrowCount, boolean hasOverdueBooks ) {
		this.id = id;
		this.email = email;
		this.userName = userName;
		this.companyId = companyId;
		this.role = role;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.activeAt = activeAt;
		this.profileImage = profileImage;
		this.provider = provider;
		this.currentBorrowCount = currentBorrowCount;
		this.hasOverdueBooks = hasOverdueBooks;
	}

	public static UserInformationForAdminResponse from(
			UserEntity user,
			int currentBorrowCount,
			boolean hasOverdueBooks
	) {
			return UserInformationForAdminResponse.builder()
					.id(user.getId())
					.email(user.getEmail())
					.userName(user.getUserName())
					.companyId(user.getCompanyId())
					.role(user.getRole().toString())
					.createdAt(user.getCreatedAt())
					.updatedAt(user.getUpdatedAt())
					.activeAt(user.getActiveAt())
					.profileImage(user.getProfileImage())
					.provider(user.getProvider().toString())
					.currentBorrowCount(currentBorrowCount)
					.hasOverdueBooks(hasOverdueBooks)
					.build();
	}
}