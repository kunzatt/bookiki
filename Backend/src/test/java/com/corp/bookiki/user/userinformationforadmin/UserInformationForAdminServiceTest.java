package com.corp.bookiki.user.userinformationforadmin;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.UserInformationForAdminRequest;
import com.corp.bookiki.user.dto.UserInformationForAdminResponse;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserInformationForAdminService;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
class UserInformationForAdminServiceTest {

	@InjectMocks
	private UserInformationForAdminService userInformationForAdminService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BookHistoryService bookHistoryService;

	@Test
	@DisplayName("전체 사용자 조회 성공")
	void getUserDetails_WhenValidRequest_ThenSuccess() {
		log.info("전체 사용자 조회 테스트 시작");
		LocalDateTime now = LocalDateTime.now();
		UserEntity user = UserEntity.builder()
			.email("test@example.com")
			.userName("테스트")
			.companyId("CORP001")
			.role(Role.USER)
			.provider(Provider.BOOKIKI)
			.createdAt(now)
			.updatedAt(now)
			.activeAt(now)
			.profileImage("profile.jpg")
			.deleted(false)  // deleted 필드 추가
			.build();
		ReflectionTestUtils.setField(user, "id", 1);
		log.info("테스트용 사용자 엔티티 생성 완료: {}", user);

		// Pageable 객체 생성
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		PageRequest pageRequest = PageRequest.of(0, 10, sort);

		// Mock 동작 설정 - findByDeletedFalse 메서드에 대한 stubbing
		when(userRepository.findByDeletedFalse(any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of(user), pageRequest, 1));

		// bookHistoryService에 대한 stubbing 추가
		when(bookHistoryService.getCurrentBorrowedBooks(anyInt(), any()))
			.thenReturn(Collections.emptyList());

		log.info("Mock 레포지토리 동작 설정 완료");

		// 서비스 메서드 호출
		Page<UserInformationForAdminResponse> result = userInformationForAdminService
			.getUserDetails(0, 10, "createdAt", "DESC");

		// 검증
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
		assertThat(result.getContent().get(0).getUserName()).isEqualTo("테스트");
		assertThat(result.getContent().get(0).getCompanyId()).isEqualTo("CORP001");

		verify(userRepository).findByDeletedFalse(any(Pageable.class));
		verify(bookHistoryService, times(2)).getCurrentBorrowedBooks(anyInt(), any());
		log.info("전체 사용자 조회 테스트 완료");
	}

	@Test
	@DisplayName("개별 사용자 조회 성공")
	void getUserDetailsById_WhenValidRequest_ThenSuccess() {
		log.info("개별 사용자 조회 테스트 시작");
		LocalDateTime now = LocalDateTime.now();
		UserEntity user = UserEntity.builder()
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
		ReflectionTestUtils.setField(user, "id", 1);
		log.info("테스트용 사용자 엔티티 생성 완료: {}", user);

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		log.info("Mock 레포지토리 동작 설정 완료");

		UserInformationForAdminResponse result = userInformationForAdminService.getUserDetailsById(1);

		assertThat(result.getEmail()).isEqualTo("test@example.com");
		assertThat(result.getUserName()).isEqualTo("테스트");
		verify(userRepository, times(1)).findById(1);
		log.info("개별 사용자 조회 테스트 완료");
	}

	@Test
	@DisplayName("존재하지 않는 사용자 조회시 예외 발생")
	void getUserDetailsById_WhenUserNotFound_ThenFail() {
		log.info("존재하지 않는 사용자 조회 테스트 시작");
		when(userRepository.findById(999)).thenReturn(Optional.empty());
		log.info("Mock 레포지토리 동작 설정 완료");

		assertThatThrownBy(() -> userInformationForAdminService.getUserDetailsById(999))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
		log.info("존재하지 않는 사용자 조회 테스트 완료");
	}

	@Test
	@DisplayName("사용자 활성 시간 수정 성공")
	void updateUserActiveAt_WhenValidRequest_ThenSuccess() {
		log.info("사용자 활성 시간 수정 테스트 시작");
		LocalDateTime now = LocalDateTime.now();
		LocalDate newActiveDate = LocalDate.now();

		UserEntity user = UserEntity.builder()
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
		ReflectionTestUtils.setField(user, "id", 1);
		log.info("테스트용 사용자 엔티티 생성 완료: {}", user);

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		log.info("Mock 레포지토리 동작 설정 완료");

		UserInformationForAdminRequest request = new UserInformationForAdminRequest();
		request.setActiveAt(newActiveDate);

		UserInformationForAdminResponse result = userInformationForAdminService.updateUserActiveAt(1, request);

		// LocalDateTime을 LocalDate로 변환하여 비교
		assertThat(result.getActiveAt().toLocalDate()).isEqualTo(newActiveDate);
		verify(userRepository, times(1)).findById(1);
		log.info("사용자 활성 시간 수정 테스트 완료");
	}

	@Test
	@DisplayName("사용자 삭제(상태 변경) 성공")
	void deleteUser_WhenValidRequest_ThenSuccess() {
		log.info("사용자 삭제 테스트 시작");
		LocalDateTime now = LocalDateTime.now();

		UserEntity user = UserEntity.builder()
			.email("test@example.com")
			.userName("테스트")
			.companyId("CORP001")
			.role(Role.USER)
			.provider(Provider.BOOKIKI)
			.createdAt(now)
			.updatedAt(now)
			.activeAt(now)
			.profileImage("profile.jpg")
			.deleted(false)
			.build();
		ReflectionTestUtils.setField(user, "id", 1);
		log.info("테스트용 사용자 엔티티 생성 완료: {}", user);

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		log.info("Mock 레포지토리 동작 설정 완료");

		UserInformationForAdminResponse result = userInformationForAdminService.deleteUser(1);

		assertThat(result.getId()).isEqualTo(1);
		verify(userRepository, times(1)).findById(1);
		log.info("사용자 삭제 테스트 완료");
	}
}