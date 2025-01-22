package com.corp.bookiki.user.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
	// 자동 증가하는 기본 키
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 고유한 이메일 (null 불가)
	@Column(unique = true, nullable = false, length = 100)
	private String email;

	// 비밀번호 (null 불가)
	@Column(nullable = false)
	private String password;

	// 사용자 이름
	@Column(name = "user_name", length = 100)
	private String userName;

	// 고유한 사번 (null 불가)
	@Column(unique = true, nullable = false, length = 50)
	private String companyId;

	// 기본 사용자 역할
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role = Role.USER;

	// 생성 시간 (null 불가, 수정 불가)
	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	// 마지막 수정 시간 (null 불가)
	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	// 마지막 활동 시간
	private LocalDateTime activeAt;

	// 프로필 이미지 경로
	@Column(length = 255)
	private String profileImage;

	// 빌더 패턴을 사용한 생성자
	@Builder
	public UserEntity(String email, String password, String userName, String companyId) {
		this.email = email;
		this.password = password;
		this.userName = userName;
		this.companyId = companyId;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		this.activeAt = LocalDateTime.now();
	}
}
