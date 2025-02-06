package com.corp.bookiki.bookhistory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookhistory.dto.BookBorrowRequest;
import com.corp.bookiki.bookhistory.dto.BookBorrowResponse;
import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;
import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookBorrowService {

	private final BookHistoryRepository bookHistoryRepository;
	private final BookItemRepository bookItemRepository;
	private final UserRepository userRepository;

	public BookBorrowResponse borrowBook(BookBorrowRequest request) {
		BookItemEntity bookItem = bookItemRepository.findById(request.getBookItemId())
			.orElseThrow(() -> new BookHistoryException(ErrorCode.BOOK_ITEM_NOT_FOUND));

		UserEntity user = userRepository.findById(request.getUserId())
			.orElseThrow(() -> new BookHistoryException(ErrorCode.USER_NOT_FOUND));

		if (!bookItem.isAvailable()) {
			throw new BookHistoryException(ErrorCode.BOOK_ALREADY_BORROWED);
		}

		BookHistoryEntity history = BookHistoryEntity.createForBorrow(bookItem, user);
		bookItem.borrow();

		BookHistoryEntity savedHistory = bookHistoryRepository.save(history);

		return BookBorrowResponse.from(savedHistory);
	}
}
