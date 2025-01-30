package com.corp.bookiki.user.repository;

import com.corp.bookiki.user.entity.SecurityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityUserRepository extends JpaRepository<SecurityUser, Integer>{

    // email로 사용자 정보 가져옴
    Optional<SecurityUser> findByEmail(String email);
}
