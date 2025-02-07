package com.corp.bookiki.loanpolicy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;

public interface LoanPolicyRepository extends JpaRepository<LoanPolicyEntity, Integer> {
}
