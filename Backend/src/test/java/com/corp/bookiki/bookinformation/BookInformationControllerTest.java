package com.corp.bookiki.bookinformation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.corp.bookiki.bookinformation.controller.BookInformationController;
import com.corp.bookiki.bookinformation.dto.BookInformationResponse;
import com.corp.bookiki.bookinformation.service.BookInformationService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookInformationException;
import com.corp.bookiki.util.CookieUtil;

import lombok.extern.slf4j.Slf4j;

@WebMvcTest(BookInformationController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("도서 정보 컨트롤러 테스트")
@Slf4j
class BookInformationControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private BookInformationService bookInformationService;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
		log.info("MockMvc 설정이 완료되었습니다.");
	}

	@Test
	@WithMockUser
	@DisplayName("정상적인 ISBN으로 도서 정보 조회 시 성공")
	void getBookInformation_WhenValidIsbn_ReturnsOk() throws Exception {
		String isbn = "9788937460470";
		BookInformationResponse response = new BookInformationResponse();
		response.setIsbn(isbn);
		response.setTitle("테스트 도서");

		log.info("테스트 도서 정보 생성: ISBN={}", isbn);
		given(bookInformationService.addBookInformationByIsbn(isbn)).willReturn(response);

		mockMvc.perform(get("/api/books/info/isbn/{isbn}", isbn))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isbn").value(isbn));

		log.info("도서 정보 조회 테스트 완료");
	}

	@Test
	@WithMockUser
	@DisplayName("잘못된 ISBN으로 조회 시 400 에러 반환")
	void getBookInformation_WhenInvalidIsbn_ReturnsBadRequest() throws Exception {
		String isbn = "invalid-isbn";
		given(bookInformationService.addBookInformationByIsbn(anyString()))
			.willThrow(new BookInformationException(ErrorCode.INVALID_ISBN));

		mockMvc.perform(get("/api/books/info/isbn/{isbn}", isbn))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	@DisplayName("도서 정보를 찾을 수 없을 때 404 에러 반환")
	void getBookInformation_WhenNotFound_ReturnsNotFound() throws Exception {
		String isbn = "9788937460470";
		given(bookInformationService.addBookInformationByIsbn(anyString()))
			.willThrow(new BookInformationException(ErrorCode.BOOK_INFO_NOT_FOUND));

		mockMvc.perform(get("/api/books/info/isbn/{isbn}", isbn))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	@DisplayName("id로 도서 정보 조회 시 성공")
	void getBookInformation_WhenValidId_ReturnsOk() throws Exception {
		int id = 1;
		BookInformationResponse response = new BookInformationResponse();
		response.setId(id);
		response.setTitle("테스트 도서");

		given(bookInformationService.getBookInformation(id)).willReturn(response);

		mockMvc.perform(get("/api/books/info/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id));
	}
}