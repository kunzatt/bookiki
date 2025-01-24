package com.corp.bookiki.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.corp.bookiki.user.dto.UserInformationForAdminResponse;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserInformationForAdminServiceTest {

	@InjectMocks
	private UserInformationForAdminService userInformationForAdminService;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("전체 사용자 조회 성공")
	void getUserDetailsSuccess() {
		// given
		LocalDateTime now = LocalDateTime.now();
		UserEntity user = UserEntity.builder()
			.id(1)
			.email("test@example.com")
			.userName("테스트")
			.companyId("CORP001")
			.role(Role.USER)
			.provider(Provider.BOOKIKI)
			.createdAt(now)
			.updatedAt(now)
			.activeAt(now)
			.profileImage("profile.jpg")
			.build();

		when(userRepository.findAll()).thenReturn(List.of(user));

		// when
		List<UserInformationForAdminResponse> result = userInformationForAdminService.getUserDetails();

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
		assertThat(result.get(0).getUserName()).isEqualTo("테스트");
		assertThat(result.get(0).getCompanyId()).isEqualTo("CORP001");
		verify(userRepository, times(1)).findAll();
	}
}