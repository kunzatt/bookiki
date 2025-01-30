package com.corp.bookiki.user.repository;

import com.corp.bookiki.user.entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.security.AuthProvider;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	// 이메일 존재 여부 확인 메서드
	boolean existsByEmail(String email);

	// 사번 존재 여부 확인 메서드
	boolean existsByCompanyId(String companyId);

	Optional<UserEntity> findByEmail(String email);
	Optional<UserEntity> findByEmailAndProvider(String email, AuthProvider provider);

	@Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.emailVerified = true")
	Optional<UserEntity> findByVerifiedEmail(@Param("email") String email);
}
