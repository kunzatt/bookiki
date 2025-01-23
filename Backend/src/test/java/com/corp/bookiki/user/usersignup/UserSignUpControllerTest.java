package com.corp.bookiki.user.usersignup;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.controller.UserSignUpController;
import com.corp.bookiki.user.dto.UserSignUpRequest;
import com.corp.bookiki.user.service.UserSignUpService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@WebMvcTest(UserSignUpController.class)
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("회원가입 컨트롤러 테스트")
@Slf4j
class UserSignUpControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private UserSignUpService userSignUpService;

	@Autowired
	private ObjectMapper objectMapper;

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
	@DisplayName("정상적인 회원가입 요청 시 성공")
	void registerWithEmail_WhenValidRequest_ReturnsOk() throws Exception {
		// given
		UserSignUpRequest request = new UserSignUpRequest();
		request.setEmail("test@test.com");
		request.setPassword("password123");
		request.setUserName("Test User");
		request.setCompanyId("EMP123");

		log.info("테스트 요청 생성: {}", request);

		// when
		doNothing().when(userSignUpService).signUp(any(UserSignUpRequest.class));
		log.info("Mock 서비스 동작 설정 완료");

		// then
		mockMvc.perform(post("/user/signup")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(content().string("회원가입이 완료되었습니다."));

		log.info("회원가입 테스트 성공적으로 완료");
	}

	@Test
	@WithMockUser
	@DisplayName("중복되지 않은 이메일 확인 시 성공")
	void checkEmailDuplicate_WhenNonDuplicateEmail_ReturnsOk() throws Exception {
		// given & when
		String email = "test@test.com";
		doNothing().when(userSignUpService).checkEmailDuplicate(email);
		log.info("이메일 중복 확인 테스트 시작: {}", email);

		// then
		mockMvc.perform(get("/user/signup/email/check")
				.param("email", email))
			.andExpect(status().isOk())
			.andExpect(content().string("이메일이 중복 되지 않습니다."));

		log.info("이메일 중복 확인 테스트 완료");
	}

	@Test
	@WithMockUser
	@DisplayName("중복되지 않은 사원번호 확인 시 성공")
	void checkCompanyId_WhenNonDuplicateId_ReturnsOk() throws Exception {
		// given
		String companyId = "EMP123";
		doNothing().when(userSignUpService).checkEmployeeIdDuplicate(companyId);
		log.info("사번 중복 확인 테스트 시작: {}", companyId);

		// then
		mockMvc.perform(get("/user/signup/company-id/check")
				.param("companyId", companyId))
			.andExpect(status().isOk())
			.andExpect(content().string("사번이 중복 되지 않습니다."));

		log.info("사번 중복 확인 테스트 완료");
	}

	@Test
	@WithMockUser
	@DisplayName("유효하지 않은 회원가입 요청 시 BAD_REQUEST 반환")
	void registerWithEmail_WhenInvalidRequest_ReturnsBadRequest() throws Exception {
		// given
		UserSignUpRequest request = new UserSignUpRequest();
		log.info("유효하지 않은 요청 테스트 시작");

		doThrow(new UserException(ErrorCode.INVALID_INPUT_VALUE))
			.when(userSignUpService).signUp(any(UserSignUpRequest.class));
		log.info("Mock 서비스에 UserException 발생 설정 완료");

		// then
		mockMvc.perform(post("/user/signup")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		log.info("유효하지 않은 요청 테스트 완료");
	}
}