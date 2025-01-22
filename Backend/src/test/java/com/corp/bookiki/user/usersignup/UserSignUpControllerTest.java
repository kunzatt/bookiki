package com.corp.bookiki.user.usersignup;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
	private MockMvc mockMvc;

	@MockBean
	private UserSignUpService userSignUpService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void registerWithEmail_WithValidRequest_ReturnsOk() throws Exception {
		// given
		UserSignUpRequest request = new UserSignUpRequest();
		request.setEmail("test@test.com");
		request.setPassword("password123");
		request.setUserName("Test User");
		request.setCompanyId("EMP123");
		log.info("회원가입 요청 생성: email={}, companyId={}", request.getEmail(), request.getCompanyId());

		String requestJson = objectMapper.writeValueAsString(request);
		log.debug("요청 JSON 변환: {}", requestJson);

		// when
		doNothing().when(userSignUpService).signUp(any(UserSignUpRequest.class));

		log.info("회원가입 API 호출 시작");
		ResultActions result = mockMvc.perform(post("/user/signup")
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestJson));

		// then
		result.andExpect(status().isOk())
			.andExpect(content().string("회원가입이 완료되었습니다."));
		log.info("회원가입 API 호출 성공");
	}

	@Test
	void checkEmailDuplicate_WithNonDuplicateEmail_ReturnsOk() throws Exception {
		// given
		String email = "test@test.com";
		log.info("이메일 중복 확인 요청: {}", email);

		doNothing().when(userSignUpService).checkEmailDuplicate(email);
		log.info("Mock 서비스 설정 완료");

		// when
		log.info("이메일 중복 확인 API 호출 시작");
		ResultActions result = mockMvc.perform(get("/user/signup/email/check")
			.param("email", email));

		// then
		result.andExpect(status().isOk())
			.andExpect(content().string("이메일이 중복 되지 않습니다."));
		log.info("이메일 중복 확인 API 호출 성공");
	}

	@Test
	void checkCompanyId_WithNonDuplicateId_ReturnsOk() throws Exception {
		// given
		String companyId = "EMP123";
		log.info("사원번호 중복 확인 요청: {}", companyId);

		doNothing().when(userSignUpService).checkEmployeeIdDuplicate(companyId);
		log.info("Mock 서비스 설정 완료");

		// when
		log.info("사원번호 중복 확인 API 호출 시작");
		ResultActions result = mockMvc.perform(get("/user/signup/company-id/check")
			.param("companyId", companyId));

		// then
		result.andExpect(status().isOk())
			.andExpect(content().string("사번이 중복 되지 않습니다."));
		log.info("사원번호 중복 확인 API 호출 성공");
	}

	@Test
	void registerWithEmail_WithInvalidRequest_ReturnsBadRequest() throws Exception {
		// given
		UserSignUpRequest request = new UserSignUpRequest();
		log.info("유효하지 않은 회원가입 요청 생성 (이메일 누락)");

		String requestJson = objectMapper.writeValueAsString(request);
		log.debug("요청 JSON 변환: {}", requestJson);

		// when
		doThrow(new UserException(ErrorCode.UNAUTHORIZED))
			.when(userSignUpService).signUp(any(UserSignUpRequest.class));

		log.info("회원가입 API 호출 시작");
		ResultActions result = mockMvc.perform(post("/user/signup")
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestJson));

		// then
		result.andExpect(status().isBadRequest());
		log.info("예상된 BadRequest 응답 확인");
	}
}