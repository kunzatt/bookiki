package com.corp.bookiki.loanpolicy;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.loanpolicy.controller.LoanPolicyController;
import com.corp.bookiki.loanpolicy.dto.LoanPolicyResponse;
import com.corp.bookiki.loanpolicy.service.LoanPolicyService;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@WebMvcTest(LoanPolicyController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("대출 정책 컨트롤러 테스트")
@Slf4j
class LoanPolicyControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	@MockBean
	private LoanPolicyService loanPolicyService;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
		log.info("MockMvc 설정이 완료되었습니다.");
	}

	@Test
	@WithMockUser
	@DisplayName("대출 정책 조회 성공")
	void getCurrentPolicy_ReturnsOk() throws Exception {
		// given
		LoanPolicyResponse response = createDefaultPolicyResponse();
		given(loanPolicyService.getCurrentPolicy()).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/loan-policy"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.maxBooks").value(5))
			.andExpect(jsonPath("$.loanPeriod").value(14));
	}

	@Test
	@WithMockUser
	@DisplayName("대출 가능 도서 수 수정 성공")
	void updateMaxBooks_WhenValid_ReturnsOk() throws Exception {
		// given
		String requestBody = """
            {
                "maxBooks": 10
            }
            """;

		// when & then
		mockMvc.perform(patch("/api/loan-policy/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	@DisplayName("대출 기간 수정 성공")
	void updateLoanPeriod_WhenValid_ReturnsOk() throws Exception {
		// given
		String requestBody = """
            {
                "loanPeriod": 21
            }
            """;

		// when & then
		mockMvc.perform(patch("/api/loan-policy/period")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	@DisplayName("전체 대출 정책 수정 성공")
	void updatePolicy_WhenValid_ReturnsOk() throws Exception {
		// given
		String requestBody = """
            {
                "maxBooks": 10,
                "loanPeriod": 21
            }
            """;

		// when & then
		mockMvc.perform(put("/api/loan-policy")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk());
	}

	private LoanPolicyResponse createDefaultPolicyResponse() {
		LoanPolicyResponse response = new LoanPolicyResponse();
		response.setMaxBooks(5);
		response.setLoanPeriod(14);
		return response;
	}
}