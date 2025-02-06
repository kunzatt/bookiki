package com.corp.bookiki.shelf;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.ShelfException;
import com.corp.bookiki.shelf.controller.ShelfController;
import com.corp.bookiki.shelf.dto.ShelfCreateRequest;
import com.corp.bookiki.shelf.dto.ShelfResponse;
import com.corp.bookiki.shelf.dto.ShelfUpdateRequest;
import com.corp.bookiki.shelf.service.ShelfService;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@WebMvcTest(ShelfController.class)
@MockBean(JpaMetamodelMappingContext.class)
//@AutoConfigureMockMvc(addFilters = false)
//@Import(TestSecurityBeansConfig.class)
class ShelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShelfService shelfService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("전체 책장 조회 성공")
    void selectAllShelf_Success() throws Exception {
        // given
        ShelfResponse shelf1 = new ShelfResponse();
        shelf1.setId(1);
        shelf1.setShelfNumber(1);
        shelf1.setLineNumber(2);
        shelf1.setCategory(100);

        ShelfResponse shelf2 = new ShelfResponse();
        shelf2.setId(2);
        shelf2.setShelfNumber(2);
        shelf2.setLineNumber(3);
        shelf2.setCategory(200);

        List<ShelfResponse> responses = Arrays.asList(shelf1, shelf2);

        when(shelfService.selectAllShelf()).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].shelfNumber").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].shelfNumber").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("책장 생성 성공")
    void createShelf_Success() throws Exception {
        // given
        ShelfCreateRequest request = new ShelfCreateRequest();
        request.setShelfNumber(1);
        request.setLineNumber(2);
        request.setCategory(100);

        when(shelfService.createShelf(any(ShelfCreateRequest.class))).thenReturn(1);

        // when & then
        mockMvc.perform(post("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("잘못된 책장 번호로 생성 실패")
    void createShelf_WithInvalidShelfNumber_ReturnsBadRequest() throws Exception {
        // given
        ShelfCreateRequest request = new ShelfCreateRequest();
        request.setShelfNumber(0);
        request.setLineNumber(2);
        request.setCategory(100);

        when(shelfService.createShelf(any(ShelfCreateRequest.class)))
                .thenThrow(new ShelfException(ErrorCode.INVALID_SHELF_NUMBER));

        // when & then
        mockMvc.perform(post("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 책장 번호입니다"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("책장 수정 성공")
    void updateShelf_Success() throws Exception {
        // given
        ShelfUpdateRequest request = new ShelfUpdateRequest();
        request.setId(1);
        request.setShelfNumber(2);
        request.setLineNumber(3);
        request.setCategory(200);

        doNothing().when(shelfService).updateShelf(any(ShelfUpdateRequest.class));

        // when & then
        mockMvc.perform(put("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("존재하지 않는 책장 수정 시도시 404 반환")
    void updateShelf_WithNonExistentId_ReturnsNotFound() throws Exception {
        // given
        ShelfUpdateRequest request = new ShelfUpdateRequest();
        request.setId(999);
        request.setShelfNumber(2);
        request.setLineNumber(3);
        request.setCategory(200);

        doThrow(new ShelfException(ErrorCode.SHELF_NOT_FOUND))
                .when(shelfService).updateShelf(any(ShelfUpdateRequest.class));

        // when & then
        mockMvc.perform(put("/api/admin/shelf/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("책장을 찾을 수 없습니다"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("책장 삭제 성공")
    void deleteShelf_Success() throws Exception {
        // given
        int id = 1;
        doNothing().when(shelfService).deleteShelf(id);

        // when & then
        mockMvc.perform(delete("/api/admin/shelf/categories/{id}", id)  // URL 패턴 수정
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())  // 실패 시 결과를 상세히 출력
                .andExpect(status().isNoContent());

        verify(shelfService).deleteShelf(id);  // service 메서드 호출 검증
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("존재하지 않는 책장 삭제 시도시 404 반환")
    void deleteShelf_WithNonExistentId_ReturnsNotFound() throws Exception {
        // given
        int id = 999;
        doThrow(new ShelfException(ErrorCode.SHELF_NOT_FOUND))
                .when(shelfService).deleteShelf(id);

        // when & then
        mockMvc.perform(delete("/api/admin/shelf/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("책장을 찾을 수 없습니다"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("책장 삭제 시 서버 오류 발생")
    void deleteShelf_WhenServerError_Returns500() throws Exception {
        // given
        int id = 1;
        doThrow(new ShelfException(ErrorCode.INTERNAL_SERVER_ERROR))
                .when(shelfService).deleteShelf(id);

        // when & then
        mockMvc.perform(delete("/api/admin/shelf/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다"));
    }
}