package com.corp.bookiki.favorite.service;

import java.awt.print.Book;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.favorite.dto.BookFavoriteRequest;
import com.corp.bookiki.favorite.dto.BookFavoriteResponse;
import com.corp.bookiki.favorite.entity.FavoriteEntity;
import com.corp.bookiki.favorite.repository.FavoriteRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;
import com.corp.bookiki.global.error.exception.FavoriteException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookFavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final UserRepository userRepository;
	private final BookItemRepository bookItemRepository;

	// 특정 유저의 전체 좋아요 조회
	@Transactional(readOnly = true)
	public Page<BookFavoriteResponse> getUserFavorites(Integer userId, Pageable pageable) {
		return favoriteRepository.findByUserIdWithBookInformation(userId, pageable)
			.map(BookFavoriteResponse::from);
	}
	
	// 특정 책의 좋아요한 유저 개수 조회
	@Transactional(readOnly = true)
	public int getBookFavoriteCount(Integer bookItemId) {
		return favoriteRepository.countByBookItemId(bookItemId);
	}
	
	// 특정 유저의 좋아요한 책의 개수 조회
	@Transactional(readOnly = true)
	public int getUserFavoriteCount(Integer userId) {
		return favoriteRepository.countByUserId(userId);
	}
	
	// 특정 유저의 특정 책 좋아요 여부 조회
	@Transactional(readOnly = true)
	public boolean checkFavorite(Integer userId, Integer bookItemId) {
		return favoriteRepository.existsByUserIdAndBookItemId(userId, bookItemId);
	}

	// 특정 책의 모든 유저 ID 조회(알림 기능에 사용 유력)
	@Transactional(readOnly = true)
	public List<Integer> getAllFavoriteUserId(Integer userId, Integer bookItemId) {
		return favoriteRepository.findUserIdByBookItemId(bookItemId);
	}

	// 좋아요 추가
	@Transactional
	public BookFavoriteResponse addFavorite(Integer userId, Integer bookItemId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

		BookItemEntity bookItemEntity = bookItemRepository.findById(bookItemId)
			.orElseThrow(() -> new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND));

		FavoriteEntity favoriteEntity = FavoriteEntity.create(userEntity, bookItemEntity);
		favoriteRepository.save(favoriteEntity);
		return BookFavoriteResponse.from(favoriteEntity);
	}

	// 좋아요 삭제
	@Transactional
	public void deleteFavorite(Integer userId, Integer bookItemId) {
		if(!favoriteRepository.existsByUserIdAndBookItemId(userId, bookItemId)) {
			throw new FavoriteException(ErrorCode.FAVORITE_NOT_FOUND);
		}
		favoriteRepository.deleteByUserIdAndBookItemId(userId, bookItemId);
	}

	// 좋아요 여부 확인 후 좋아요 취소 or 좋아요 동작
	@Transactional
	public BookFavoriteResponse toggleFavorite(Integer userId, Integer bookItemId) {
		boolean isFavorite = checkFavorite(userId, bookItemId);

		if(isFavorite) {
			deleteFavorite(userId, bookItemId);
			return null;
		}

		return addFavorite(userId, bookItemId);
	}
}
