package com.corp.bookiki.loanpolicy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.corp.bookiki.global.error.exception.LoanPolicyException;
import com.corp.bookiki.loanpolicy.dto.LoanBookPolicyRequest;
import com.corp.bookiki.loanpolicy.dto.LoanPeriodPolicyRequest;
import com.corp.bookiki.loanpolicy.dto.LoanPolicyRequest;
import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;
import com.corp.bookiki.loanpolicy.repository.LoanPolicyRepository;
import com.corp.bookiki.loanpolicy.service.LoanPolicyService;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
class LoanPolicyServiceTest {

	@InjectMocks
	private LoanPolicyService loanPolicyService;

	@Mock
	private LoanPolicyRepository loanPolicyRepository;

	@Nested
	@DisplayName("대출 정책 조회 테스트")
	class GetCurrentPolicy {

		@Test
		@DisplayName("현재 대출 정책 조회 성공")
		void getCurrentPolicy_WhenExists_ThenSuccess() {
			// given
			LoanPolicyEntity policy = createDefaultPolicy();
			given(loanPolicyRepository.findById(1)).willReturn(Optional.of(policy));

			// when
			var result = loanPolicyService.getCurrentPolicy();

			// then
			assertNotNull(result);
			assertEquals(policy.getMaxBooks(), result.getMaxBooks());
			assertEquals(policy.getLoanPeriod(), result.getLoanPeriod());
		}

		@Test
		@DisplayName("대출 정책이 없을 때 예외 발생")
		void getCurrentPolicy_WhenNotExists_ThenThrowException() {
			// given
			given(loanPolicyRepository.findById(1)).willReturn(Optional.empty());

			// when & then
			assertThrows(LoanPolicyException.class,
				() -> loanPolicyService.getCurrentPolicy());
		}
	}

	@Nested
	@DisplayName("대출 정책 수정 테스트")
	class UpdatePolicy {

		@Test
		@DisplayName("최대 대출 가능 도서 수 수정 성공")
		void updateMaxBooks_WhenValid_ThenSuccess() {
			// given
			LoanPolicyEntity policy = createDefaultPolicy();
			given(loanPolicyRepository.findById(1)).willReturn(Optional.of(policy));

			LoanBookPolicyRequest request = new LoanBookPolicyRequest();
			request.setMaxBooks(10);

			// when
			var result = loanPolicyService.updateMaxBooks(request);

			// then
			assertEquals(10, result.getMaxBooks());
		}

		@Test
		@DisplayName("대출 기간 수정 성공")
		void updateLoanPeriod_WhenValid_ThenSuccess() {
			// given
			LoanPolicyEntity policy = createDefaultPolicy();
			given(loanPolicyRepository.findById(1)).willReturn(Optional.of(policy));

			LoanPeriodPolicyRequest request = new LoanPeriodPolicyRequest();
			request.setLoanPeriod(21);

			// when
			var result = loanPolicyService.updateLoanPeriod(request);

			// then
			assertEquals(21, result.getLoanPeriod());
		}

		@Test
		@DisplayName("전체 대출 정책 수정 성공")
		void updatePolicy_WhenValid_ThenSuccess() {
			// given
			LoanPolicyEntity policy = createDefaultPolicy();
			given(loanPolicyRepository.findById(1)).willReturn(Optional.of(policy));

			LoanPolicyRequest request = new LoanPolicyRequest();
			request.setMaxBooks(10);
			request.setLoanPeriod(21);

			// when
			var result = loanPolicyService.updatePolicy(request);

			// then
			assertEquals(10, result.getMaxBooks());
			assertEquals(21, result.getLoanPeriod());
		}
	}

	private LoanPolicyEntity createDefaultPolicy() {
		return LoanPolicyEntity.builder()
			.maxBooks(5)
			.loanPeriod(14)
			.build();
	}
}