package com.corp.bookiki.user.repository;

import com.corp.bookiki.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.corp.bookiki.user.entity.Provider;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	// 이메일 존재 여부 확인 메서드
	boolean existsByEmail(String email);

	// 사번 존재 여부 확인 메서드
	boolean existsByCompanyId(String companyId);

	Optional<UserEntity> findByEmail(String email);
	Optional<UserEntity> findByEmailAndProvider(String email, Provider provider);


}
