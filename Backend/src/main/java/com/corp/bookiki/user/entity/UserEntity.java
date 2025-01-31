package com.corp.bookiki.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity implements OAuth2User, UserDetails {
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

	// 이메일 인증 여부
	@Column(nullable = false)
	private boolean emailVerified = false;

	//OAuth provider
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider = Provider.BOOKIKI;

	@Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean deleted = false;

	@Builder
	public UserEntity(Integer id, String email, String password, String userName, String companyId, Role role, Provider provider, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime activeAt, String profileImage, Boolean deleted) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.userName = userName;
		this.companyId = companyId;
		this.role = role;
		this.provider = provider;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.activeAt = activeAt;
		this.profileImage = profileImage;
		this.deleted = false;
	}

	// 이메일 인증 완료 처리
	public void verifyEmail() {
		this.emailVerified = true;
	}

	// 비밀번호 변경
	public void updatePassword(String newPassword) {
		this.password = newPassword;
	}

	// OAuth2 정보 업데이트
	public void updateOAuth2Info(Provider provider, String providerId) {
		this.provider = provider;
		this.providerId = providerId;
	}

	@Transient  // DB에 저장되지 않음
	private Map<String, Object> attributes;

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
	}

	@Override
	public String getName() {
		return this.email;
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public void updateActiveAt(LocalDateTime activeAt) {
		this.activeAt = activeAt;
	}

	public void delete() {
		this.deleted = true;
	}
}
