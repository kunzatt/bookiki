package com.corp.bookiki.bookitem.service;

import com.corp.bookiki.bookhistory.repository.BookHistoryRepository;
import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookinformation.entity.Category;
import com.corp.bookiki.bookinformation.repository.BookInformationRepository;
import com.corp.bookiki.bookitem.dto.*;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.bookitem.enums.SearchType;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookItemService {

	private final BookItemRepository bookItemRepository;
	private final BookInformationRepository bookInformationRepository;
	private final BookHistoryRepository bookHistoryRepository;

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

	@Transactional(readOnly = true)
	public List<BookItemListResponse> getPopularBooksByCategory(Integer category, int limit) {
		return bookItemRepository.findPopularBooks(category, BookStatus.AVAILABLE, PageRequest.of(0, limit))
			.stream()
			.map(BookItemListResponse::from)
			.collect(Collectors.toList());
	}

	public List<BookItemEntity> getBooksSameBookInformation(Integer bookItemId){
		return bookItemRepository.findByBookInformationIdFromBookItemId(bookItemId);
	}

	@Transactional(readOnly = true)
	public List<Integer> getBooksIdSameBookInformation(Integer bookItemId){
		return bookItemRepository.findIdsByBookInformationIdFromBookItemId(bookItemId);
	}

	public Page<BookAdminListResponse> selectBooksForAdmin(String keyword, int page, int size) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

		Page<BookItemEntity> bookItems = bookItemRepository.findAllBooksForAdmin(keyword, pageRequest);
		log.info("조회된 도서 수: {}", bookItems.getContent().size());

		Page<BookAdminListResponse> response = bookItems.map(item -> {
			try {
				log.debug("도서 ID {} 변환 시작", item.getId());
				BookAdminListResponse dto = new BookAdminListResponse(item);
				log.debug("도서 ID {} 변환 결과: {}", item.getId(), dto);  // 변환된 DTO 로깅
				return dto;
			} catch (Exception e) {
				log.error("도서 ID {} DTO 변환 실패: {}", item.getId(), e.getMessage());
				return null;
			}
		});

		log.info("변환된 DTO 수: {}", response.getContent().size());  // 최종 응답 크기 로깅
		return response;
	}

	@Transactional
	public void updateBookStatus(Integer id, BookStatus newStatus) {
		BookItemEntity bookItem = bookItemRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));

		if (newStatus == BookStatus.AVAILABLE) {
			bookItem.returnBook();
		} else if (newStatus == BookStatus.UNAVAILABLE) {
			bookItem.markAsLost();
		}
	}


	// AI 추천용 메서드
	@Transactional(readOnly = true)
	public List<BookItemListResponse> getRecommendedBooksByKeywords(List<String> keywords, int limit) {
		Set<BookItemListResponse> recommendations = new LinkedHashSet<>();

		for (String keyword : keywords) {
			List<BookItemEntity> books = bookItemRepository.findRecommendedBooksByKeyword(
					keyword,
					PageRequest.of(0, limit)
			);

			recommendations.addAll(books.stream()
					.map(BookItemListResponse::from)
					.collect(Collectors.toList()));

			if (recommendations.size() >= limit) {
				break;
			}
		}

		// 추천 도서가 없을 경우 예외 발생
		if (recommendations.isEmpty()) {
			throw new BookItemException(ErrorCode.BOOK_SEARCH_NOT_FOUND);
		}

		return recommendations.stream()
				.limit(limit)
				.collect(Collectors.toList());
	}

	public BookAdminDetailResponse getBookAdminDetail(Integer bookItemId) {
		// 1. BookItem 조회 (연관된 BookInformation도 함께 조회)
		BookItemEntity bookItem = bookItemRepository.findById(bookItemId)
				.orElseThrow(() -> new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND));

		// 2. BookInformation 정보 접근
		BookInformationEntity bookInfo = bookItem.getBookInformation();

		// 일단 여기까지 작성해서 기본 정보들이 잘 조회되는지 테스트해볼까요?
		return BookAdminDetailResponse.builder()
				.title(bookInfo.getTitle())
				.author(bookInfo.getAuthor())
				.publisher(bookInfo.getPublisher())
				.isbn(bookInfo.getIsbn())
				.publishedAt(bookInfo.getPublishedAt())
				.image(bookInfo.getImage())
				.description(bookInfo.getDescription())
				.category(Category.ofCode(bookInfo.getCategory()))
				.id(bookItem.getId())
				.purchaseAt(bookItem.getPurchaseAt())
				.bookStatus(bookItem.getBookStatus())
				.build();
	}
}
