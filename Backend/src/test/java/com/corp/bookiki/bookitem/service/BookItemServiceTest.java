package com.corp.bookiki.bookitem.service;

import com.corp.bookiki.bookitem.dto.BookItemDisplayResponse;
import com.corp.bookiki.bookitem.dto.BookItemResponse;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.bookitem.repository.BookItemRepository;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BookItemServiceTest {

	@InjectMocks
	private BookItemService bookItemService;

	@Mock
	private BookItemRepository bookItemRepository;

	@Nested
	@DisplayName("도서 아이템 목록 조회 테스트")
	class GetAllBookItems {
		@Test
		void getAllBookItems_ShouldReturnPagedResults() {
			// given
			int page = 0;
			int size = 10;
			String sortBy = "id";
			String direction = "desc";
			String keyword = "test";

			PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
			BookItemEntity mockEntity = BookItemEntity.builder()
				.purchaseAt(LocalDateTime.now())
				.bookStatus(BookStatus.AVAILABLE)
				.deleted(false)
				.build();
			ReflectionTestUtils.setField(mockEntity, "id", 1);

			Page<BookItemEntity> mockPage = new PageImpl<>(List.of(mockEntity));

			given(bookItemRepository.findByDeletedFalse(pageRequest)).willReturn(mockPage);
			log.info("Mock 설정 완료: 페이지네이션된 도서 아이템 목록");

			// when
			Page<BookItemDisplayResponse> result = bookItemService.selectBooksByKeyword(page, size, sortBy, direction, keyword);
			log.info("도서 아이템 목록 조회 결과: size={}", result.getContent().size());

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).isNotEmpty();
			// verify도 findByDeletedFalse()로 수정
			verify(BookItemDisplayResponse).findAllWithKeyword(pageRequest);
			log.info("도서 아이템 목록 조회 테스트 성공");
		}

		@Nested
		@DisplayName("도서 아이템 단건 조회 테스트")
		class GetBookItemById {
			@Test
			void getBookItemById_WhenExists_ShouldReturnBookItem() {
				// given
				Integer id = 1;
				BookItemEntity mockBookItem = BookItemEntity.builder()
					.purchaseAt(LocalDateTime.now())
					.bookStatus(BookStatus.AVAILABLE)
					.deleted(false)
					.build();

				ReflectionTestUtils.setField(mockBookItem, "id", 1);

				given(bookItemRepository.findById(id)).willReturn(Optional.of(mockBookItem));
				log.info("Mock 설정 완료: ID로 도서 아이템 조회");

				// when
				BookItemResponse result = bookItemService.getBookItemById(id);
				log.info("도서 아이템 단건 조회 결과: id={}", id);

				// then
				assertThat(result).isNotNull();
				verify(bookItemRepository).findById(id);
				log.info("도서 아이템 단건 조회 테스트 성공");
			}

			@Test
			void getBookItemById_WhenNotExists_ShouldThrowException() {
				// given
				Integer id = 1;
				given(bookItemRepository.findById(id)).willReturn(Optional.empty());
				log.info("Mock 설정 완료: 존재하지 않는 도서 아이템");

				// when & then
				assertThatThrownBy(() -> bookItemService.getBookItemById(id))
					.isInstanceOf(BookItemException.class);
				log.info("존재하지 않는 도서 아이템 조회 예외 테스트 성공");
			}

			@Test
			@DisplayName("존재하는 도서 아이템 삭제 시 성공")
			void deleteBookItem_WhenExists_ShouldDeleteSuccessfully() {
				// given
				Integer id = 1;
				BookItemEntity mockBookItem = BookItemEntity.builder()
					.purchaseAt(LocalDateTime.now())
					.bookStatus(BookStatus.AVAILABLE)
					.deleted(false)
					.build();
				ReflectionTestUtils.setField(mockBookItem, "id", 1);

				given(bookItemRepository.findById(id)).willReturn(Optional.of(mockBookItem));
				log.info("Mock 설정 완료: 도서 아이템 삭제");

				// when
				bookItemService.deleteBookItem(id);
				log.info("도서 아이템 삭제 실행");

				// then
				assertThat(mockBookItem.getDeleted()).isTrue();
				verify(bookItemRepository).findById(id);
				log.info("도서 아이템 삭제 테스트 성공");
			}

			@Test
			@DisplayName("존재하지 않는 도서 아이템 삭제 시 예외 발생")
			void deleteBookItem_WhenNotExists_ShouldThrowException() {
				// given
				Integer id = 1;
				given(bookItemRepository.findById(id)).willReturn(Optional.empty());
				log.info("Mock 설정 완료: 존재하지 않는 도서 아이템");

				// when & then
				assertThatThrownBy(() -> bookItemService.deleteBookItem(id))
					.isInstanceOf(BookItemException.class);
				verify(bookItemRepository).findById(id);
				log.info("존재하지 않는 도서 아이템 삭제 예외 테스트 성공");
			}

			@Test
			@DisplayName("이미 삭제된 도서 아이템 삭제 시 예외 발생")
			void deleteBookItem_WhenAlreadyDeleted_ShouldThrowException() {
				// given
				Integer id = 1;
				BookItemEntity mockBookItem = BookItemEntity.builder()
					.purchaseAt(LocalDateTime.now())
					.bookStatus(BookStatus.AVAILABLE)
					.deleted(true)
					.build();
				ReflectionTestUtils.setField(mockBookItem, "id", 1);

				given(bookItemRepository.findById(id)).willReturn(Optional.of(mockBookItem));
				log.info("Mock 설정 완료: 이미 삭제된 도서 아이템");

				// when & then
				assertThatThrownBy(() -> bookItemService.deleteBookItem(id))
					.isInstanceOf(BookItemException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_ALREADY_DELETED);
				verify(bookItemRepository).findById(id);
				log.info("이미 삭제된 도서 아이템 삭제 예외 테스트 성공");
			}
		}
	}
}