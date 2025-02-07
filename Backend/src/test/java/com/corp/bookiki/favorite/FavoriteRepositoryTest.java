package com.corp.bookiki.favorite;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookinformation.repository.BookInformationRepository;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.favorite.entity.FavoriteEntity;
import com.corp.bookiki.favorite.repository.FavoriteRepository;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class FavoriteRepositoryTest {

	@Autowired
	private FavoriteRepository favoriteRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookItemRepository bookItemRepository;

	@Autowired
	private BookInformationRepository bookInformationRepository;

	@Test
	@DisplayName("유저의 좋아요 목록 조회 성공")
	void findByUserIdWithBookInformation_Success() {
		// given
		UserEntity user = createAndSaveUser();
		BookItemEntity bookItem = createAndSaveBookItem();
		FavoriteEntity favorite = createAndSaveFavorite(user, bookItem);

		// when
		Page<FavoriteEntity> favorites = favoriteRepository.findByUserIdWithBookInformation(
			user.getId(),
			PageRequest.of(0, 10)
		);

		// then
		assertThat(favorites).isNotEmpty();
		assertThat(favorites.getContent().get(0).getUser().getId()).isEqualTo(user.getId());
		assertThat(favorites.getContent().get(0).getBookItem().getId()).isEqualTo(bookItem.getId());
	}

	@Test
	@DisplayName("좋아요 여부 확인 성공")
	void existsByUserIdAndBookItemId_Success() {
		// given
		UserEntity user = createAndSaveUser();
		BookItemEntity bookItem = createAndSaveBookItem();
		FavoriteEntity favorite = createAndSaveFavorite(user, bookItem);

		// when
		boolean exists = favoriteRepository.existsByUserIdAndBookItemId(user.getId(), bookItem.getId());

		// then
		assertThat(exists).isTrue();
	}

	private UserEntity createAndSaveUser() {
		UserEntity user = UserEntity.builder()
			.email("test@example.com")
			.userName("테스트 유저")
			.password("1234456")
			.provider(Provider.BOOKIKI)
			.deleted(false)
			.role(Role.USER)
			.companyId("test222")
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
		return userRepository.save(user);
	}

	private BookItemEntity createAndSaveBookItem() {
		BookInformationEntity bookInfo = BookInformationEntity.builder()
			.isbn("9788937460470")
			.title("테스트 도서")
			.author("테스트 저자")
			.publishedAt(LocalDateTime.now())
			.build();
		bookInformationRepository.save(bookInfo);

		BookItemEntity bookItem = BookItemEntity.builder()
			.bookInformation(bookInfo)
			.build();
		return bookItemRepository.save(bookItem);
	}

	private FavoriteEntity createAndSaveFavorite(UserEntity user, BookItemEntity bookItem) {
		FavoriteEntity favorite = FavoriteEntity.create(user, bookItem);
		return favoriteRepository.save(favorite);
	}
}