package com.corp.bookiki.bookcheckout.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookcheckout.dto.BookBorrowRequest;
import com.corp.bookiki.bookcheckout.dto.BookBorrowResponse;
import com.corp.bookiki.bookcheckout.enitity.BookHistoryEntity;
import com.corp.bookiki.bookcheckout.repository.BookCheckOutRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookBorrowService {

	private final BookCheckOutRepository bookHistoryRepository;
	private final BookItemRepository bookItemRepository;
	private final UserRepository userRepository;

	public BookBorrowResponse borrowBook(BookBorrowRequest request) {
		BookItemEntity bookItem = bookItemRepository.findById(request.getBookItemId())
			.orElseThrow(() -> new BusinessException(ErrorCode.BOOK_ITEM_NOT_FOUND));

		UserEntity user = userRepository.findById(request.getUserId())
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		if (!bookItem.isAvailable()) {
			throw new BusinessException(ErrorCode.BOOK_ALREADY_BORROWED);
		}

		BookHistoryEntity history = new BookHistoryEntity(bookItem, user);
		bookItem.borrow();

		BookHistoryEntity savedHistory = bookHistoryRepository.save(history);

		return BookBorrowResponse.from(savedHistory);
	}
}
