package com.corp.bookiki.bookitem.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.dto.BookItemDisplayResponse;
import com.corp.bookiki.bookitem.dto.BookItemListResponse;
import com.corp.bookiki.bookitem.dto.BookItemRequest;
import com.corp.bookiki.bookitem.dto.BookItemResponse;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.bookitem.enums.SearchType;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookItemException;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@WebMvcTest(BookItemController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class,
	CurrentUserArgumentResolver.class, WebMvcConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("도서 아이템 컨트롤러 테스트")
class BookItemControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookItemService bookItemService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtService jwtService;

	private UserEntity testUserEntity;
	private UserEntity testAdminEntity;
	private String testUserEmail;
	private String testAdminEmail;

	@BeforeEach
	void setup() {
		// 일반 사용자 설정
		testUserEmail = "user@example.com";
		testUserEntity = UserEntity.builder()
			.email(testUserEmail)
			.userName("Test User")
			.role(Role.USER)
			.build();
		ReflectionTestUtils.setField(testUserEntity, "id", 2);

		// 관리자 설정
		testAdminEmail = "admin@example.com";
		testAdminEntity = UserEntity.builder()
			.email(testAdminEmail)
			.userName("Admin User")
			.role(Role.ADMIN)
			.build();
		ReflectionTestUtils.setField(testAdminEntity, "id", 1);

		// 일반 사용자 Claims 설정
		Claims userClaims = mock(Claims.class);
		given(userClaims.getSubject()).willReturn("user:" + testUserEmail);
		given(userClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 3600000));
		given(userClaims.get("authorities", String.class)).willReturn("ROLE_USER");

		// 관리자 Claims 설정
		Claims adminClaims = mock(Claims.class);
		given(adminClaims.getSubject()).willReturn("user:" + testAdminEmail);
		given(adminClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 3600000));
		given(adminClaims.get("authorities", String.class)).willReturn("ROLE_USER,ROLE_ADMIN");

		// JWT Service 모킹
		given(jwtService.validateToken(eq("user-jwt-token"))).willReturn(true);
		given(jwtService.validateToken(eq("admin-jwt-token"))).willReturn(true);
		given(jwtService.extractAllClaims(eq("user-jwt-token"))).willReturn(userClaims);
		given(jwtService.extractAllClaims(eq("admin-jwt-token"))).willReturn(adminClaims);
		given(jwtService.extractEmail(eq("user-jwt-token"))).willReturn(testUserEmail);
		given(jwtService.extractEmail(eq("admin-jwt-token"))).willReturn(testAdminEmail);
		given(jwtService.isTokenExpired(anyString())).willReturn(false);

		// UserRepository 모킹
		given(userRepository.findByEmail(eq(testUserEmail)))
			.willReturn(Optional.of(testUserEntity));
		given(userRepository.findByEmail(eq(testAdminEmail)))
			.willReturn(Optional.of(testAdminEntity));

		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
	}

	@Nested
	@DisplayName("도서 아이템 목록 조회 테스트")
	class GetAllBookItems {
		@Test
		@WithMockUser
		@DisplayName("정상적인 도서 아이템 목록 조회 시 성공")
		void getAllBookItems_WhenValidRequest_ThenReturnsOk() throws Exception {
			// given
			BookInformationEntity bookInfo = BookInformationEntity.builder()
					.title("Test Book")
					.author("Test Author")
					.isbn("1234567890")
					.build();
			ReflectionTestUtils.setField(bookInfo, "id", 1);

			BookItemEntity mockEntity = BookItemEntity.builder()
				.bookInformation(bookInfo)
				.purchaseAt(LocalDateTime.now())
				.bookStatus(BookStatus.AVAILABLE)
				.deleted(false)
				.build();

			ReflectionTestUtils.setField(mockEntity, "id", 1);
			BookItemDisplayResponse mockResponse = BookItemDisplayResponse.from(mockEntity);
			Page<BookItemDisplayResponse> mockPage = new PageImpl<>(List.of(mockResponse));

			given(bookItemService.selectBooksByKeyword(anyInt(), anyInt(), anyString(), anyString(), anyString()))
				.willReturn(mockPage);
			log.info("Mock 서비스 설정 완료: 도서 아이템 목록");

			// when & then
			mockMvc.perform(get("/api/books/search/list")
					.with(csrf())
					.param("page", "0")
					.param("size", "10")
					.param("sortBy", "id")
					.param("direction", "desc")
					.param("keyword", "test")
					.cookie(getUserJwtCookie())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

			log.info("도서 아이템 목록 조회 테스트 성공");
		}

		@Test
		@WithMockUser
		@DisplayName("검색 결과가 없을 때 NotFound 반환")
		void getAllBookItems_WhenNoResults_ThenReturnsNotFound() throws Exception {
			// given
			given(bookItemService.selectBooksByKeyword(anyInt(), anyInt(), anyString(), anyString(), anyString()))
					.willThrow(new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND));

			// when & then
			mockMvc.perform(get("/api/books/search/list")
							.with(csrf())
							.param("page", "0")
							.param("size", "10")
							.param("sortBy", "id")
							.param("direction", "desc")
							.param("keyword", "존재하지않는도서")
							.cookie(getUserJwtCookie())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound());

			log.info("검색 결과 없음 테스트 성공");
		}

	}

	@Nested
	@DisplayName("도서 아이템 검색 테스트")
	class SearchBooks {
		@Test
		@WithMockUser
		@DisplayName("정상적인 도서 아이템 검색 시 성공")
		void searchBooks_WhenValidRequest_ThenReturnsOk() throws Exception {
			// given
			BookInformationEntity bookInfo = BookInformationEntity.builder()
					.title("Test Book")
					.author("Test Author")
					.isbn("1234567890")
					.build();
			ReflectionTestUtils.setField(bookInfo, "id", 1);

			BookItemEntity mockEntity = BookItemEntity.builder()
					.bookInformation(bookInfo)
					.purchaseAt(LocalDateTime.now())
					.bookStatus(BookStatus.AVAILABLE)
					.deleted(false)
					.build();
			ReflectionTestUtils.setField(mockEntity, "id", 1);

			BookItemListResponse mockResponse = BookItemListResponse.from(mockEntity);

			Pageable pageable = PageRequest.of(0, 10);
			Page<BookItemListResponse> mockPage = new PageImpl<>(
					List.of(mockResponse),
					pageable,
					1  // total elements
			);


			given(bookItemService.selectBooks(any(SearchType.class), anyString(), anyInt(), anyInt()))
					.willReturn(mockPage);

			// when & then
			mockMvc.perform(get("/api/books/search")
							.with(csrf())
							.param("page", "0")
							.param("size", "10")
							.param("type", "TITLE")
							.param("keyword", "test")
							.cookie(getUserJwtCookie())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
		}

		@Test
		@WithMockUser
		@DisplayName("잘못된 SearchType으로 요청 시 실패")
		void searchBooks_WhenInvalidSearchType_ThenReturnsBadRequest() throws Exception {
			mockMvc.perform(get("/api/books/search")
							.with(csrf())
							.param("page", "0")
							.param("size", "10")
							.param("type", "INVALID_TYPE")
							.param("keyword", "test")
							.cookie(getUserJwtCookie())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isInternalServerError());
		}

		@Test
		@WithMockUser
		@DisplayName("키워드 없이 요청 시 전체 목록 반환")
		void searchBooks_WhenNoKeyword_ThenReturnsBadRequest() throws Exception {
			// given
			BookInformationEntity bookInfo = BookInformationEntity.builder()
					.title("Test Book")
					.author("Test Author")
					.isbn("1234567890")
					.build();
			ReflectionTestUtils.setField(bookInfo, "id", 1);

			BookItemEntity mockEntity = BookItemEntity.builder()
					.bookInformation(bookInfo)
					.purchaseAt(LocalDateTime.now())
					.bookStatus(BookStatus.AVAILABLE)
					.deleted(false)
					.build();
			ReflectionTestUtils.setField(mockEntity, "id", 1);

			BookItemListResponse mockResponse = BookItemListResponse.from(mockEntity);
			Pageable pageable = PageRequest.of(0, 10);
			Page<BookItemListResponse> mockPage = new PageImpl<>(
					List.of(mockResponse),
					pageable,
					1
			);

			// keyword가 null인 경우의 서비스 동작 모킹
			given(bookItemService.selectBooks(any(SearchType.class), isNull(), anyInt(), anyInt()))
					.willReturn(mockPage);

			// when & then
			mockMvc.perform(get("/api/books/search")
							.with(csrf())
							.param("page", "0")
							.param("size", "10")
							.param("type", "TITLE")
							.cookie(getUserJwtCookie())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
		}

		@Test
		@WithMockUser
		@DisplayName("검색 결과가 없을 때 NotFound 반환")
		void searchBooks_WhenNoResults_ThenReturnsNotFound() throws Exception {
			// given
			given(bookItemService.selectBooks(any(SearchType.class), anyString(), anyInt(), anyInt()))
					.willThrow(new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND));

			// when & then
			mockMvc.perform(get("/api/books/search")
							.with(csrf())
							.param("page", "0")
							.param("size", "10")
							.param("type", "TITLE")
							.param("keyword", "존재하지않는도서")
							.cookie(getUserJwtCookie())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound());

			log.info("검색 결과 없음 테스트 성공");
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
			BookItemResponse mockResponse = BookItemResponse.from(mockEntity);

			given(bookItemService.getBookItemById(id)).willReturn(mockResponse);
			log.info("Mock 서비스 설정 완료: 도서 아이템 단건 조회");

			// when & then
			mockMvc.perform(get("/api/books/search/qrcodes/{id}", id)
					.with(csrf())
					.cookie(getUserJwtCookie())
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
					.cookie(getUserJwtCookie())
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
					.cookie(getUserJwtCookie())
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
		void deleteBookItem_WhenValidRequest_ThenReturnsOk() throws Exception {
			// given
			Integer id = 1;
			BookItemEntity mockEntity = BookItemEntity.builder()
				.purchaseAt(LocalDateTime.now())
				.bookStatus(BookStatus.AVAILABLE)
				.deleted(true)
				.build();
			ReflectionTestUtils.setField(mockEntity, "id", id);
			BookItemResponse mockResponse = BookItemResponse.from(mockEntity);

			given(bookItemService.deleteBookItem(id)).willReturn(mockResponse);
			log.info("Mock 서비스 설정 완료: 도서 아이템 삭제");

			// when & then
			mockMvc.perform(delete("/api/books/search/{id}", id)
					.with(csrf())
					.cookie(getUserJwtCookie())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

			log.info("도서 아이템 삭제 테스트 성공");
		}

		@Test
		@WithMockUser
		@DisplayName("존재하지 않는 도서 아이템 삭제 시 NotFound 반환")
		void deleteBookItem_WhenNotFound_ThenReturnsNotFound() throws Exception {
			// given
			Integer id = 999;
			given(bookItemService.deleteBookItem(id))
				.willThrow(new BookItemException(ErrorCode.BOOK_ITEM_NOT_FOUND));
			log.info("Mock 서비스 설정 완료: 존재하지 않는 도서 아이템 삭제");

			// when & then
			mockMvc.perform(delete("/api/books/search/{id}", id)
					.with(csrf())
					.cookie(getUserJwtCookie())
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
			given(bookItemService.deleteBookItem(id))
				.willThrow(new BookItemException(ErrorCode.BOOK_ALREADY_DELETED));
			log.info("Mock 서비스 설정 완료: 이미 삭제된 도서 아이템");

			// when & then
			mockMvc.perform(delete("/api/books/search/{id}", id)
					.with(csrf())
					.cookie(getUserJwtCookie())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

			log.info("이미 삭제된 도서 아이템 삭제 테스트 성공");
		}
	}

	@Nested
	@DisplayName("도서 아이템 등록 테스트")
	class AddBookItem {
		@Test
		@WithMockUser(roles = "ADMIN")
		@DisplayName("정상적인 도서 아이템 등록 시 성공")
		void addBookItem_WhenValidRequest_ThenReturnsOk() throws Exception {
			// given
			Integer id = 1;
			LocalDateTime purchaseAt = LocalDateTime.now();
			BookItemRequest request = BookItemRequest.builder()
				.bookInformationId(id)
				.purchaseAt(purchaseAt)
				.build();

			BookItemEntity mockEntity = BookItemEntity.builder()
				.purchaseAt(purchaseAt)
				.bookStatus(BookStatus.AVAILABLE)
				.deleted(false)
				.build();
			ReflectionTestUtils.setField(mockEntity, "id", id);
			BookItemResponse mockResponse = BookItemResponse.from(mockEntity);

			given(bookItemService.addBookItem(any(BookItemRequest.class))).willReturn(mockResponse);
			log.info("Mock 서비스 설정 완료: 도서 아이템 등록");

			// when & then
			mockMvc.perform(post("/api/admin/books/search")
					.with(csrf())
					.cookie(getAdminJwtCookie())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

			log.info("도서 아이템 등록 테스트 성공");
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		@DisplayName("도서 정보를 찾을 수 없을 때 에러 반환")
		void addBookItem_WhenBookInfoNotFound_ThenReturnsBadRequest() throws Exception {
			// given
			Integer id = 999;
			BookItemRequest request = BookItemRequest.builder()
				.bookInformationId(id)
				.purchaseAt(LocalDateTime.now())
				.build();

			given(bookItemService.addBookItem(any(BookItemRequest.class)))
				.willThrow(new BookItemException(ErrorCode.BOOK_INFO_NOT_FOUND));
			log.info("Mock 서비스 설정 완료: 존재하지 않는 도서 정보");

			// when & then
			mockMvc.perform(post("/api/admin/books/search")
					.with(csrf())
					.cookie(getAdminJwtCookie())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());

			log.info("존재하지 않는 도서 정보로 도서 아이템 등록 테스트 성공");
		}
	}

	private Cookie getUserJwtCookie() {
		Cookie accessTokenCookie = new Cookie("access_token", "user-jwt-token");
		accessTokenCookie.setPath("/");
		return accessTokenCookie;
	}

	private Cookie getAdminJwtCookie() {
		Cookie accessTokenCookie = new Cookie("access_token", "admin-jwt-token");
		accessTokenCookie.setPath("/");
		return accessTokenCookie;
	}
}