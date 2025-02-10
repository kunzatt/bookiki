package com.corp.bookiki.loanpolicy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;

public interface LoanPolicyRepository extends JpaRepository<LoanPolicyEntity, Integer> {
	Optional<LoanPolicyEntity> findFirstByOrderByIdDesc();
}
