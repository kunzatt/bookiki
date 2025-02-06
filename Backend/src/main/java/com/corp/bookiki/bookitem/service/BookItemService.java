package com.corp.bookiki.bookitem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookinformation.repository.BookInformationRepository;
import com.corp.bookiki.bookitem.dto.BookItemRequest;
import com.corp.bookiki.bookitem.dto.BookItemResponse;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookItemService {

	private final BookItemRepository bookItemRepository;
	private final BookInformationRepository bookInformationRepository;

	@Transactional
	public Page<BookItemResponse> getAllBookItems(int page, int size, String sortBy, String direction) {
		Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
		PageRequest pageRequest = PageRequest.of(page, size, sort);

		Page<BookItemEntity> bookItem = bookItemRepository.findByDeletedFalse(pageRequest);
		return bookItem.map(BookItemResponse::from);
	}

	@Transactional
	public BookItemResponse getBookItemById(Integer id) {
		BookItemEntity bookItem = bookItemRepository.findById(id)
			.orElseThrow(() -> new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND));

		if (bookItem.getDeleted()) {
			throw new BookItemException(ErrorCode.BOOK_ALREADY_DELETED);
		}
		return BookItemResponse.from(bookItem);  // 여기도 수정
	}

	@Transactional
	public BookItemResponse deleteBookItem(Integer id) {
		BookItemEntity bookItem = bookItemRepository.findById(id)
			.orElseThrow(() -> new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND));

		if (bookItem.getDeleted()) {
			throw new BookItemException(ErrorCode.BOOK_ALREADY_DELETED);
		}

		bookItem.delete();
		return BookItemResponse.from(bookItem);
	}

	@Transactional
	public BookItemResponse addBookItem(BookItemRequest bookItemRequest) {
		BookInformationEntity bookInformation = bookInformationRepository.findById(bookItemRequest.getBookInformationId())
			.orElseThrow(() -> new BookItemException(ErrorCode.BOOK_INFO_NOT_FOUND));

		BookItemEntity bookItem = BookItemEntity.builder()
			.bookInformation(bookInformation)
			.purchaseAt(bookItemRequest.getPurchaseAt())
			.build();

		BookItemEntity savedBookItem = bookItemRepository.save(bookItem);

		return BookItemResponse.from(savedBookItem);
	}
}
