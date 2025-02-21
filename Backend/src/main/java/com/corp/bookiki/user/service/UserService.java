package com.corp.bookiki.user.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.UserInformationForAdminRequest;
import com.corp.bookiki.user.dto.UserInformationForAdminResponse;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public UserEntity getUserById(Integer userId) {
		UserEntity user = userRepository.findById(userId).orElse(null);
		if(user == null) throw new UserException(ErrorCode.USER_NOT_FOUND);
		return user;
	}

	@Transactional(readOnly = true)
	public List<UserEntity> getUsersByIds(List<Integer> ids) {
		List<UserEntity> users = userRepository.findAllById(ids);
		if(users.size() != ids.size()) {
			throw new UserException(ErrorCode.USER_NOT_FOUND);
		}
		return users;
	}

	@Transactional(readOnly = true)
	public List<UserEntity> getUsersByRole(Role role) {
		List<UserEntity> users = userRepository.findAllByRole(role);
		if(role == Role.ADMIN) {
			if(users.isEmpty()) throw new UserException(ErrorCode.ADMIN_NOT_FOUND);
		}else {
			if(users.isEmpty()) throw new UserException(ErrorCode.USER_NOT_FOUND);
		}
		return users;
	}
}