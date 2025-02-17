package com.corp.bookiki.favorite;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.favorite.dto.BookFavoriteResponse;
import com.corp.bookiki.favorite.entity.FavoriteEntity;
import com.corp.bookiki.favorite.repository.FavoriteRepository;
import com.corp.bookiki.favorite.service.BookFavoriteService;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BookFavoriteServiceTest {

	@InjectMocks
	private BookFavoriteService bookFavoriteService;

	@Mock
	private FavoriteRepository favoriteRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BookItemService bookItemService;

	@Mock
	private BookItemRepository bookItemRepository;

	@Nested
	@DisplayName("좋아요 토글 테스트")
	class ToggleFavorite {

		@Test
		@DisplayName("좋아요 추가 성공")
		void toggleFavorite_AddSuccess() {
			// given
			Integer userId = 2;
			Integer bookItemId = 100;
			UserEntity user = createUser(userId);
			BookItemEntity bookItem = createBookItem(bookItemId);
			BookItemEntity sameBookItem = createBookItem(101); // 같은 bookInformation을 가진 다른 bookItem

			given(favoriteRepository.existsByUserIdAndBookItemId(userId, bookItemId))
				.willReturn(false);
			given(userRepository.findById(userId))
				.willReturn(Optional.of(user));
			given(bookItemService.getBooksSameBookInformation(bookItemId))
				.willReturn(List.of(bookItem, sameBookItem));

			// when
			String result = bookFavoriteService.toggleFavorite(userId, bookItemId);

			// then
			assertThat(result).isEqualTo("좋아요 추가");
			verify(favoriteRepository).saveAll(anyList());
			verify(bookItemService).getBooksSameBookInformation(bookItemId);
		}

		@Test
		@DisplayName("좋아요 취소 성공")
		void toggleFavorite_DeleteSuccess() {
			// given
			Integer userId = 2;
			Integer bookItemId = 100;
			List<Integer> bookItemIds = List.of(100, 101); // 같은 bookInformation을 가진 bookItem들의 ID

			given(favoriteRepository.existsByUserIdAndBookItemId(userId, bookItemId))
				.willReturn(true);
			given(bookItemService.getBooksIdSameBookInformation(bookItemId))
				.willReturn(bookItemIds);

			// when
			String result = bookFavoriteService.toggleFavorite(userId, bookItemId);

			// then
			assertThat(result).isEqualTo("좋아요 삭제");
			verify(favoriteRepository).deleteByUserIdAndBookItemIdIn(userId, bookItemIds);
			verify(bookItemService).getBooksIdSameBookInformation(bookItemId);
		}
	}

	@Test
	@DisplayName("좋아요 목록 조회 성공")
	void getUserFavorites_Success() {
		// given
		UserEntity user = createUser(2);
		BookItemEntity bookItem = createBookItem(100);
		FavoriteEntity favorite = createFavorite(user, bookItem);

		Page<FavoriteEntity> favoritePage = new PageImpl<>(
			List.of(favorite),
			PageRequest.of(0, 10),
			1
		);

		given(favoriteRepository.findByUserIdWithBookInformation(eq(2), any()))
			.willReturn(favoritePage);

		// when
		Page<BookFavoriteResponse> response = bookFavoriteService.getUserFavorites(2, PageRequest.of(0, 10));

		// then
		assertThat(response).isNotNull();
		assertThat(response.getContent()).hasSize(1);
		assertThat(response.getContent().get(0).getUserId()).isEqualTo(2);
	}

	private UserEntity createUser(Integer id) {
		UserEntity user = UserEntity.builder()
			.email("test@example.com")
			.userName("테스트 유저")
			.role(Role.USER)
			.createdAt(LocalDateTime.now())
			.build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private BookItemEntity createBookItem(Integer id) {
		BookInformationEntity bookInfo = BookInformationEntity.builder()
			.isbn("9788937460470")
			.title("테스트 도서")
			.author("테스트 저자")
			.publishedAt(LocalDateTime.now())
			.build();

		BookItemEntity bookItem = BookItemEntity.builder()
			.bookInformation(bookInfo)
			.build();
		ReflectionTestUtils.setField(bookItem, "id", id);
		return bookItem;
	}

	private FavoriteEntity createFavorite(UserEntity user, BookItemEntity bookItem) {
		return FavoriteEntity.create(user, bookItem);
	}
}