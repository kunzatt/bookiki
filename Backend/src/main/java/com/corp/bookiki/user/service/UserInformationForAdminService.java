package com.corp.bookiki.user.service;

import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.UserInformationForAdminRequest;
import com.corp.bookiki.user.dto.UserInformationForAdminResponse;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInformationForAdminService {

	private final UserRepository userRepository;
	private final BookHistoryService bookHistoryService;

	@Transactional(readOnly = true)
	public Page<UserInformationForAdminResponse> getUserDetails(int page, int size, String sortBy, String direction) {
		Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
		PageRequest pageRequest = PageRequest.of(page, size, sort);

		Page<UserEntity> users = userRepository.findByDeletedFalse(pageRequest);

		if (users.isEmpty()) {
			throw new UserException(ErrorCode.USER_NOT_FOUND);
		}

		return users.map(userEntity -> {
			List<BookHistoryResponse> currentBooks =
					bookHistoryService.getCurrentBorrowedBooks(userEntity.getId(), null);

			List<BookHistoryResponse> overdueBooks =
					bookHistoryService.getCurrentBorrowedBooks(userEntity.getId(), true);

			return UserInformationForAdminResponse.from(
					userEntity,
					currentBooks.size(),
					!overdueBooks.isEmpty()
			);
		});
	}

	@Transactional(readOnly = true)
	public UserInformationForAdminResponse getUserDetailsById(Integer id) {
		UserEntity user = userRepository.findById(id)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

		if (user.getDeleted()) {
			throw new UserException(ErrorCode.USER_NOT_FOUND);
		}

		List<BookHistoryResponse> currentBooks =
				bookHistoryService.getCurrentBorrowedBooks(user.getId(), null);

		List<BookHistoryResponse> overdueBooks =
				bookHistoryService.getCurrentBorrowedBooks(user.getId(), true);

		return UserInformationForAdminResponse.from(
				user,
				currentBooks.size(),
				!overdueBooks.isEmpty()
		);
	}

	@Transactional
	public UserInformationForAdminResponse updateUserActiveAt(Integer id, UserInformationForAdminRequest request) {
		UserEntity user = userRepository.findById(id)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

		if (user.getDeleted()) {
			throw new UserException(ErrorCode.USER_NOT_FOUND);
		}

		user.updateActiveAt(request.getActiveAt());
		List<BookHistoryResponse> currentBooks =
				bookHistoryService.getCurrentBorrowedBooks(user.getId(), null);
		List<BookHistoryResponse> overdueBooks =
				bookHistoryService.getCurrentBorrowedBooks(user.getId(), true);

		return UserInformationForAdminResponse.from(
				user,
				currentBooks.size(),
				!overdueBooks.isEmpty()
		);
	}

	@Transactional
	public UserInformationForAdminResponse deleteUser(Integer id) {
		UserEntity user = userRepository.findById(id)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

		if (user.getDeleted()) {
			throw new UserException(ErrorCode.USER_NOT_FOUND);
		}

		user.delete();
		List<BookHistoryResponse> currentBooks =
				bookHistoryService.getCurrentBorrowedBooks(user.getId(), null);
		List<BookHistoryResponse> overdueBooks =
				bookHistoryService.getCurrentBorrowedBooks(user.getId(), true);

		return UserInformationForAdminResponse.from(
				user,
				currentBooks.size(),
				!overdueBooks.isEmpty()
		);
	}
}