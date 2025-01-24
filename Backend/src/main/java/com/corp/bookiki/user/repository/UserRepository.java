package com.corp.bookiki.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corp.bookiki.user.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	// 이메일 존재 여부 확인 메서드
	boolean existsByEmail(String email);

	// 사번 존재 여부 확인 메서드
	boolean existsByCompanyId(String companyId);

	// 전체 회원 조회
	List<UserEntity> findAll();

	// 회원별 상세 조회
	Optional<UserEntity> findById(Integer id);

}
