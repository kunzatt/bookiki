package com.corp.bookiki.bookitem.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookitem.dto.BookItemResponse;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(BookItemController.class)
@AutoConfigureMockMvc
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@DisplayName("도서 아이템 컨트롤러 테스트")
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
class BookItemControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private BookItemService bookItemService;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
		log.info("MockMvc 설정이 완료되었습니다.");
	}

	@Nested
	@DisplayName("도서 아이템 목록 조회 테스트")
	class GetAllBookItems {
		@Test
		@WithMockUser
		@DisplayName("정상적인 도서 아이템 목록 조회 시 성공")
		void getAllBookItems_WhenValidRequest_ThenReturnsOk() throws Exception {
			// given
			BookItemEntity mockEntity = BookItemEntity.builder()
				.purchaseAt(LocalDateTime.now())
				.bookStatus(BookStatus.AVAILABLE)
				.deleted(false)
				.build();
			ReflectionTestUtils.setField(mockEntity, "id", 1);
			BookItemResponse mockResponse = new BookItemResponse(mockEntity);
			Page<BookItemResponse> mockPage = new PageImpl<>(List.of(mockResponse));

			given(bookItemService.getAllBookItems(anyInt(), anyInt(), anyString(), anyString()))
				.willReturn(mockPage);
			log.info("Mock 서비스 설정 완료: 도서 아이템 목록");

			// when & then
			mockMvc.perform(get("/api/books/search/list")
					.with(csrf())
					.param("page", "0")
					.param("size", "10")
					.param("sortBy", "id")
					.param("direction", "desc")
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

			log.info("도서 아이템 목록 조회 테스트 성공");
		}

	}

	@Nested
	@DisplayName("도서 아이템 단건 조회 테스트")
	class GetBookItemById {
		@Test
		@WithMockUser
		@DisplayName("정상적인 도서 아이템 단건 조회 시 성공")
		void getBookItemById_WhenValidRequest_ThenReturnsOk() throws Exception {
			// given
			Integer id = 1;
			BookItemEntity mockEntity = BookItemEntity.builder()
				.purchaseAt(LocalDateTime.now())
				.bookStatus(BookStatus.AVAILABLE)
				.deleted(false)
				.build();
			ReflectionTestUtils.setField(mockEntity, "id", 1);
			BookItemResponse mockResponse = new BookItemResponse(mockEntity);

			given(bookItemService.getBookItemById(id)).willReturn(mockResponse);
			log.info("Mock 서비스 설정 완료: 도서 아이템 단건 조회");

			// when & then
			mockMvc.perform(get("/api/books/search/qrcodes/{id}", id)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

			log.info("도서 아이템 단건 조회 테스트 성공");
		}

		@Test
		@WithMockUser
		@DisplayName("존재하지 않는 도서 아이템 조회 시 NotFound 반환")
		void getBookItemById_WhenNotFound_ThenReturnsNotFound() throws Exception {
			// given
			Integer id = 999;
			given(bookItemService.getBookItemById(id))
				.willThrow(new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND));
			log.info("Mock 서비스 설정 완료: 존재하지 않는 도서 아이템");

			// when & then
			mockMvc.perform(get("/api/books/search/qrcodes/{id}", id)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

			log.info("존재하지 않는 도서 아이템 조회 테스트 성공");
		}

		@Test
		@WithMockUser
		@DisplayName("이미 삭제된 도서 아이템 조회 시 에러 반환")
		void getBookItemById_WhenDeleted_ThenReturnsError() throws Exception {
			// given
			Integer id = 1;
			given(bookItemService.getBookItemById(id))
				.willThrow(new BookItemException(ErrorCode.BOOK_ALREADY_DELETED));
			log.info("Mock 서비스 설정 완료: 삭제된 도서 아이템");

			// when & then
			mockMvc.perform(get("/api/books/search/qrcodes/{id}", id)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

			log.info("삭제된 도서 아이템 조회 테스트 성공");
		}
	}

	@Nested
	@DisplayName("도서 아이템 삭제 테스트")
	class DeleteBookItem {
		@Test
		@WithMockUser
		@DisplayName("정상적인 도서 아이템 삭제 시 성공")
		void deleteBookItem_WhenValidRequest_ThenReturnsNoContent() throws Exception {
			// given
			Integer id = 1;
			when(bookItemService.deleteBookItem(id)).thenReturn("삭제 되었습니다.");
			log.info("Mock 서비스 설정 완료: 도서 아이템 삭제");

			// when & then
			mockMvc.perform(delete("/api/books/search/qrcodes/{id}", id)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("삭제 되었습니다."));

			log.info("도서 아이템 삭제 테스트 성공");
		}

		@Test
		@WithMockUser
		@DisplayName("존재하지 않는 도서 아이템 삭제 시 NotFound 반환")
		void deleteBookItem_WhenNotFound_ThenReturnsNotFound() throws Exception {
			// given
			Integer id = 999;
			willThrow(new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND))
				.given(bookItemService).deleteBookItem(id);
			log.info("Mock 서비스 설정 완료: 존재하지 않는 도서 아이템 삭제");

			// when & then
			mockMvc.perform(delete("/api/books/search/qrcodes/{id}", id)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

			log.info("존재하지 않는 도서 아이템 삭제 테스트 성공");
		}

		@Test
		@WithMockUser
		@DisplayName("이미 삭제된 도서 아이템 삭제 시 에러 반환")
		void deleteBookItem_WhenAlreadyDeleted_ThenReturnsBadRequest() throws Exception {
			// given
			Integer id = 1;
			willThrow(new BookItemException(ErrorCode.BOOK_ALREADY_DELETED))
				.given(bookItemService).deleteBookItem(id);
			log.info("Mock 서비스 설정 완료: 이미 삭제된 도서 아이템");

			// when & then
			mockMvc.perform(delete("/api/books/search/qrcodes/{id}", id)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

			log.info("이미 삭제된 도서 아이템 삭제 테스트 성공");
		}
	}
}