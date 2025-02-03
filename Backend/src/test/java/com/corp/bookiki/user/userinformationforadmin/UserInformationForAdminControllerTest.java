package com.corp.bookiki.user.userinformationforadmin;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.jwt.JwtTokenProvider;
import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.controller.UserInformationForAdminController;
import com.corp.bookiki.user.dto.UserInformationForAdminRequest;
import com.corp.bookiki.user.dto.UserInformationForAdminResponse;
import com.corp.bookiki.user.service.UserInformationForAdminService;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(UserInformationForAdminController.class)
@Import({SecurityConfig.class, CookieUtil.class})
@AutoConfigureMockMvc(addFilters = false)
class UserInformationForAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserInformationForAdminService userInformationForAdminService;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private JWTService jwtService;

	@MockBean
	private UserDetailsService userDetailsService;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	private UserInformationForAdminResponse createTestResponse(LocalDateTime now) {
		return UserInformationForAdminResponse.builder()
			.id(1)
			.email("test@example.com")
			.userName("테스트")
			.companyId("CORP001")
			.role("USER")
			.createdAt(now)
			.updatedAt(now)
			.activeAt(now)
			.profileImage("profile.jpg")
			.provider("BOOKIKI")
			.build();
	}

	@Test
	@DisplayName("전체 사용자 조회 API 성공")
	@WithMockUser(roles = "ADMIN")
	void getUserDetails_WhenValidRequest_ThenSuccess() throws Exception {
		log.info("전체 사용자 조회 API 테스트 시작");
		LocalDateTime now = LocalDateTime.now();
		UserInformationForAdminResponse response = createTestResponse(now);
		log.info("테스트용 응답 객체 생성 완료: {}", response);

		when(userInformationForAdminService.getUserDetails()).thenReturn(List.of(response));
		log.info("Mock 서비스 동작 설정 완료");

		mockMvc.perform(get("/admin/users")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].email").value("test@example.com"))
			.andExpect(jsonPath("$[0].userName").value("테스트"))
			.andExpect(jsonPath("$[0].companyId").value("CORP001"));

		verify(userInformationForAdminService, times(1)).getUserDetails();
		log.info("전체 사용자 조회 API 테스트 완료");
	}

	@Test
	@DisplayName("개별 사용자 조회 API 성공")
	@WithMockUser(roles = "ADMIN")
	void getUserDetailsById_WhenValidRequest_ThenSuccess() throws Exception {
		log.info("개별 사용자 조회 API 테스트 시작");
		LocalDateTime now = LocalDateTime.now();
		UserInformationForAdminResponse response = createTestResponse(now);
		log.info("테스트용 응답 객체 생성 완료: {}", response);

		when(userInformationForAdminService.getUserDetailsById(1)).thenReturn(response);
		log.info("Mock 서비스 동작 설정 완료");

		mockMvc.perform(get("/admin/users/1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("test@example.com"))
			.andExpect(jsonPath("$.userName").value("테스트"))
			.andExpect(jsonPath("$.companyId").value("CORP001"));

		verify(userInformationForAdminService, times(1)).getUserDetailsById(1);
		log.info("개별 사용자 조회 API 테스트 완료");
	}

	@Test
	@DisplayName("사용자 활성 시간 수정 API 성공")
	@WithMockUser(roles = "ADMIN")
	void updateUserActiveAt_WhenValidRequest_ThenSuccess() throws Exception {
		log.info("사용자 활성 시간 수정 API 테스트 시작");
		LocalDateTime now = LocalDateTime.now();
		UserInformationForAdminResponse response = createTestResponse(now);
		log.info("테스트용 응답 객체 생성 완료: {}", response);

		UserInformationForAdminRequest request = new UserInformationForAdminRequest();
		request.setActiveAt(now.plusHours(1));

		when(userInformationForAdminService.updateUserActiveAt(eq(1), any(UserInformationForAdminRequest.class)))
			.thenReturn(response);
		log.info("Mock 서비스 동작 설정 완료");

		mockMvc.perform(put("/admin/users/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.email").value("test@example.com"))
			.andExpect(jsonPath("$.userName").value("테스트"))
			.andExpect(jsonPath("$.companyId").value("CORP001"));

		verify(userInformationForAdminService, times(1))
			.updateUserActiveAt(eq(1), any(UserInformationForAdminRequest.class));
		log.info("사용자 활성 시간 수정 API 테스트 완료");
	}

	@Test
	@DisplayName("사용자 삭제 API 성공")
	@WithMockUser(roles = "ADMIN")
	void deleteUser_WhenValidRequest_ThenSuccess() throws Exception {
		log.info("사용자 삭제 API 테스트 시작");
		LocalDateTime now = LocalDateTime.now();
		UserInformationForAdminResponse response = createTestResponse(now);
		log.info("테스트용 응답 객체 생성 완료: {}", response);

		when(userInformationForAdminService.deleteUser(1)).thenReturn(response);
		log.info("Mock 서비스 동작 설정 완료");

		mockMvc.perform(delete("/admin/users/1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.email").value("test@example.com"))
			.andExpect(jsonPath("$.userName").value("테스트"))
			.andExpect(jsonPath("$.companyId").value("CORP001"));

		verify(userInformationForAdminService, times(1)).deleteUser(1);
		log.info("사용자 삭제 API 테스트 완료");
	}
}