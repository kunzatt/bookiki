package com.corp.bookiki.loanpolicy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.LoanPolicyException;
import com.corp.bookiki.loanpolicy.dto.LoanBookPolicyRequest;
import com.corp.bookiki.loanpolicy.dto.LoanBookPolicyResponse;
import com.corp.bookiki.loanpolicy.dto.LoanPeriodPolicyRequest;
import com.corp.bookiki.loanpolicy.dto.LoanPeriodPolicyResponse;
import com.corp.bookiki.loanpolicy.dto.LoanPolicyRequest;
import com.corp.bookiki.loanpolicy.dto.LoanPolicyResponse;
import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;
import com.corp.bookiki.loanpolicy.repository.LoanPolicyRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanPolicyService {

	private static final Integer DEFAULT_POLICY_ID = 1;
	private final LoanPolicyRepository loanPolicyRepository;

	@Transactional(readOnly = true)
	public LoanPolicyResponse getCurrentPolicy() {
		LoanPolicyEntity policy = findPolicyById(DEFAULT_POLICY_ID);
		return new LoanPolicyResponse(policy);
	}

	@Transactional
	public LoanBookPolicyResponse updateMaxBooks(@Valid LoanBookPolicyRequest request) {
		LoanPolicyEntity policy = findPolicyById(DEFAULT_POLICY_ID);
		policy.updateMaxBooks(request.getMaxBooks());
		return new LoanBookPolicyResponse(policy);
	}

	@Transactional
	public LoanPeriodPolicyResponse updateLoanPeriod(@Valid LoanPeriodPolicyRequest request) {
		LoanPolicyEntity policy = findPolicyById(DEFAULT_POLICY_ID);
		policy.updateLoanPeriod(request.getLoanPeriod());
		return new LoanPeriodPolicyResponse(policy);
	}

	@Transactional
	public LoanPolicyResponse updatePolicy(@Valid LoanPolicyRequest request) {
		LoanPolicyEntity policy = findPolicyById(DEFAULT_POLICY_ID);
		policy.updatePolicy(request.getMaxBooks(), request.getLoanPeriod());
		return new LoanPolicyResponse(policy);
	}

	private LoanPolicyEntity findPolicyById(Integer id) {
		return loanPolicyRepository.findById(id)
			.orElseThrow(() -> {
				return new LoanPolicyException(ErrorCode.LOAN_POLICY_NOT_FOUND);
			});
	}
}