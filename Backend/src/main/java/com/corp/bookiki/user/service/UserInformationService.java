package com.corp.bookiki.user.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.user.dto.UserInformationResponse;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInformationService {

	private final UserService userService;
	private final BookHistoryService bookHistoryService;

	@Transactional(readOnly = true)
	public UserInformationResponse getUserInformation(Integer userId) {
		UserEntity userEntity = userService.getUserById(userId);
		if(userEntity.getActiveAt().isAfter(LocalDateTime.now()))
			return UserInformationResponse.from(userEntity, 0);
		Integer availableLoans = bookHistoryService.countCanBorrowBook(userId);
		return UserInformationResponse.from(userEntity, availableLoans);
	}
}
