package com.corp.bookiki.user.repository;

import com.corp.bookiki.user.entity.SecurityUser;
import com.corp.bookiki.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	// 이메일 존재 여부 확인 메서드
	boolean existsByEmail(String email);

	// 사번 존재 여부 확인 메서드
	boolean existsByCompanyId(String companyId);

	// email로 사용자 정보 가져옴
	Optional<SecurityUser> findByEmail(String email);
}
