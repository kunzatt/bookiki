package com.corp.bookiki.loanpolicy;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.Optional;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.loanpolicy.controller.LoanPolicyController;
import com.corp.bookiki.loanpolicy.dto.LoanPolicyResponse;
import com.corp.bookiki.loanpolicy.service.LoanPolicyService;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@WebMvcTest(LoanPolicyController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
	CurrentUserArgumentResolver.class, WebMvcConfig.class})
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

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtService jwtService;

	private UserEntity testUserEntity;
	private UserEntity testAdminEntity;
	private String testUserEmail;
	private String testAdminEmail;

	@BeforeEach
	void setup() {
		// 일반 사용자 설정
		testUserEmail = "user@example.com";
		testUserEntity = UserEntity.builder()
			.email(testUserEmail)
			.userName("Test User")
			.role(Role.USER)
			.build();
		ReflectionTestUtils.setField(testUserEntity, "id", 2);

		// 관리자 설정
		testAdminEmail = "admin@example.com";
		testAdminEntity = UserEntity.builder()
			.email(testAdminEmail)
			.userName("Admin User")
			.role(Role.ADMIN)
			.build();
		ReflectionTestUtils.setField(testAdminEntity, "id", 1);

		// 일반 사용자 Claims 설정
		Claims userClaims = mock(Claims.class);
		given(userClaims.getSubject()).willReturn("user:" + testUserEmail);
		given(userClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 3600000));
		given(userClaims.get("authorities", String.class)).willReturn("ROLE_USER");

		// 관리자 Claims 설정
		Claims adminClaims = mock(Claims.class);
		given(adminClaims.getSubject()).willReturn("user:" + testAdminEmail);
		given(adminClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 3600000));
		given(adminClaims.get("authorities", String.class)).willReturn("ROLE_USER,ROLE_ADMIN");

		// JWT Service 모킹
		given(jwtService.validateToken(eq("user-jwt-token"))).willReturn(true);
		given(jwtService.validateToken(eq("admin-jwt-token"))).willReturn(true);
		given(jwtService.extractAllClaims(eq("user-jwt-token"))).willReturn(userClaims);
		given(jwtService.extractAllClaims(eq("admin-jwt-token"))).willReturn(adminClaims);
		given(jwtService.extractEmail(eq("user-jwt-token"))).willReturn(testUserEmail);
		given(jwtService.extractEmail(eq("admin-jwt-token"))).willReturn(testAdminEmail);
		given(jwtService.isTokenExpired(anyString())).willReturn(false);

		// UserRepository 모킹
		given(userRepository.findByEmail(eq(testUserEmail)))
			.willReturn(Optional.of(testUserEntity));
		given(userRepository.findByEmail(eq(testAdminEmail)))
			.willReturn(Optional.of(testAdminEntity));

		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
	}

	@Test
	@WithMockUser()
	@DisplayName("대출 정책 조회 성공")
	void getCurrentPolicy_ReturnsOk() throws Exception {
		// given
		LoanPolicyResponse response = createDefaultPolicyResponse();
		given(loanPolicyService.getCurrentPolicy()).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/loan-policy")
				.cookie(getUserJwtCookie()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.maxBooks").value(5))
			.andExpect(jsonPath("$.loanPeriod").value(14));
	}

	@Test
	@WithMockUser()
	@DisplayName("대출 가능 도서 수 수정 성공")
	void updateMaxBooks_WhenValid_ReturnsOk() throws Exception {
		// given
		String requestBody = """
            {
                "maxBooks": 10
            }
            """;

		// when & then
		mockMvc.perform(patch("/api/admin/loan-policy/books")
				.cookie(getAdminJwtCookie())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser()
	@DisplayName("대출 기간 수정 성공")
	void updateLoanPeriod_WhenValid_ReturnsOk() throws Exception {
		// given
		String requestBody = """
            {
                "loanPeriod": 21
            }
            """;

		// when & then
		mockMvc.perform(patch("/api/admin/loan-policy/period")
				.cookie(getAdminJwtCookie())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser()
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
		mockMvc.perform(put("/api/admin/loan-policy")
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(getAdminJwtCookie())
				.content(requestBody))
			.andExpect(status().isOk());
	}

	private LoanPolicyResponse createDefaultPolicyResponse() {
		LoanPolicyResponse response = new LoanPolicyResponse();
		response.setMaxBooks(5);
		response.setLoanPeriod(14);
		return response;
	}

	private Cookie getUserJwtCookie() {
		Cookie accessTokenCookie = new Cookie("access_token", "user-jwt-token");
		accessTokenCookie.setPath("/");
		return accessTokenCookie;
	}

	private Cookie getAdminJwtCookie() {
		Cookie accessTokenCookie = new Cookie("access_token", "admin-jwt-token");
		accessTokenCookie.setPath("/");
		return accessTokenCookie;
	}
}