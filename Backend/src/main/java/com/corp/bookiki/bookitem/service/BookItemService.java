package com.corp.bookiki.bookitem.service;

import com.corp.bookiki.bookitem.dto.BookItemDisplayResponse;
import com.corp.bookiki.bookitem.dto.BookItemListResponse;
import com.corp.bookiki.bookitem.dto.BookItemResponse;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.enums.SearchType;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookItemService {

	private final BookItemRepository bookItemRepository;

	@Transactional
	public Page<BookItemDisplayResponse> selectBooksByKeyword(int page, int size, String sortBy, String direction, String keyword) {
		Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
		PageRequest pageRequest = PageRequest.of(page, size, sort);

		Page<BookItemEntity> books = bookItemRepository.findAllWithKeyword(keyword, pageRequest);

		if(books.isEmpty()){
			throw new BookItemException(ErrorCode.BOOK_SEARCH_NOT_FOUND);
		}
		return books.map(BookItemDisplayResponse::from);
	}

	@Transactional(readOnly = true)
	public Page<BookItemListResponse> selectBooks(SearchType type, String keyword, int page, int size) {
		PageRequest pageRequest = PageRequest.of(page, size);
		Page<BookItemEntity> books;

		// keyword가 없으면 전체 목록 조회
		if (keyword == null || keyword.trim().isEmpty()) {
			books = bookItemRepository.findAll(pageRequest);
		} else {
			books = bookItemRepository.searchBooks(type.name(), keyword, pageRequest);
		}

		if(books.isEmpty()){
			throw new BookItemException(ErrorCode.BOOK_SEARCH_NOT_FOUND);
		}

		return books.map(BookItemListResponse::from);
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
}
