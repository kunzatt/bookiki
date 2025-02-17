package com.corp.bookiki.user.repository;

import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	// 이메일 존재 여부 확인 메서드
	boolean existsByEmail(String email);

	// 사번 존재 여부 확인 메서드
	boolean existsByCompanyId(String companyId);

	Optional<UserEntity> findByEmail(String email);
	Optional<UserEntity> findByEmailAndProvider(String email, Provider provider);

	List<UserEntity> findAllByRole(Role role);

	// 삭제되지 않은 사용자 페이지네이션 조회
	Page<UserEntity> findByDeletedFalse(Pageable pageable);

}
