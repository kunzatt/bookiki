package com.corp.bookiki.user.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.UserInformationForAdminRequest;
import com.corp.bookiki.user.dto.UserInformationForAdminResponse;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInformationForAdminService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<UserInformationForAdminResponse> getUserDetails() {
		return userRepository.findAll().stream()
			.map(UserInformationForAdminResponse::from)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public UserInformationForAdminResponse getUserDetailsById(Integer id) {
		return UserInformationForAdminResponse.from(
			userRepository.findById(id)
				.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND))
		);
	}

	@Transactional
	public UserInformationForAdminResponse updateUserActiveAt(Integer id, UserInformationForAdminRequest request) {
		UserEntity user = userRepository.findById(id)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

		user.updateActiveAt(request.getActiveAt());
		return UserInformationForAdminResponse.from(user);
	}

}