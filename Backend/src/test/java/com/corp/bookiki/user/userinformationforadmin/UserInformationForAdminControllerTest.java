package com.corp.bookiki.user.userinformationforadmin;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.user.controller.UserInformationForAdminController;
import com.corp.bookiki.user.dto.UserInformationForAdminResponse;
import com.corp.bookiki.user.service.UserInformationForAdminService;

@WebMvcTest(UserInformationForAdminController.class)
@Import(SecurityConfig.class)
class UserInformationForAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserInformationForAdminService userInformationForAdminService;

	@Test
	@DisplayName("전체 사용자 조회 API 성공")
	@WithMockUser(roles = "ADMIN")
	void getUserDetailsApiSuccess() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		UserInformationForAdminResponse response = UserInformationForAdminResponse.builder()
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

		when(userInformationForAdminService.getUserDetails()).thenReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/admin/users")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].email").value("test@example.com"))
			.andExpect(jsonPath("$[0].userName").value("테스트"))
			.andExpect(jsonPath("$[0].companyId").value("CORP001"));

		verify(userInformationForAdminService, times(1)).getUserDetails();
	}
}