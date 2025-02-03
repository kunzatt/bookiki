package com.corp.bookiki.qna;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaCommentException;
import com.corp.bookiki.qna.controller.QnaCommentController;
import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.service.QnaCommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(QnaCommentController.class)
@AutoConfigureMockMvc(addFilters = false)  // 시큐리티 필터 비활성화
class QnaCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QnaCommentService qnaCommentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // ObjectMapper LocalDateTime 직렬화 설정
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("댓글 등록 성공 테스트")
    void createQnaComment() throws Exception {
        // given
        QnaCommentRequest request = new QnaCommentRequest();
        ReflectionTestUtils.setField(request, "qnaId", 1);
        ReflectionTestUtils.setField(request, "content", "Test Comment");

        when(qnaCommentService.createQnaComment(any(QnaCommentRequest.class), anyInt()))
                .thenReturn(1);

        // when & then
        mockMvc.perform(post("/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("댓글 수정 성공 테스트")
    void updateQnaComment() throws Exception {
        // given
        QnaCommentUpdate request = new QnaCommentUpdate();
        ReflectionTestUtils.setField(request, "id", 1);
        ReflectionTestUtils.setField(request, "content", "Updated Comment");

        // when & then
        mockMvc.perform(put("/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("문의사항 답변이 수정되었습니다."));

        verify(qnaCommentService).updateQnaComment(any(QnaCommentUpdate.class));
    }

    @Test
    @DisplayName("댓글 삭제 성공 테스트")
    void deleteQnaComment() throws Exception {
        // given
        int commentId = 1;

        // when & then
        mockMvc.perform(delete("/admin/qna/{id}", commentId))
                .andExpect(status().isNoContent())
                .andExpect(content().string("문의사항 답변이 삭제되었습니다."));

        verify(qnaCommentService).deleteQnaComment(commentId);
    }

    @Test
    @DisplayName("댓글 등록 실패 테스트 - 유효성 검증 실패")
    void createQnaComment_ValidationFail() throws Exception {
        // given
        QnaCommentRequest request = new QnaCommentRequest();
        ReflectionTestUtils.setField(request, "qnaId", 1);
        ReflectionTestUtils.setField(request, "content", ""); // 빈 내용

        // when & then
        mockMvc.perform(post("/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("잘못된 입력값입니다"));
    }

    @Test
    @DisplayName("댓글 등록 실패 테스트 - 서비스 예외 발생")
    void createQnaComment_ServiceException() throws Exception {
        // given
        QnaCommentRequest request = new QnaCommentRequest();
        ReflectionTestUtils.setField(request, "qnaId", 1);
        ReflectionTestUtils.setField(request, "content", "Test Comment");

        when(qnaCommentService.createQnaComment(any(QnaCommentRequest.class), anyInt()))
                .thenThrow(new QnaCommentException(ErrorCode.INTERNAL_SERVER_ERROR));

        // when & then
        mockMvc.perform(post("/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("댓글 수정 실패 테스트 - 존재하지 않는 댓글")
    void updateQnaComment_NotFound() throws Exception {
        // given
        QnaCommentUpdate request = new QnaCommentUpdate();
        ReflectionTestUtils.setField(request, "id", 999);
        ReflectionTestUtils.setField(request, "content", "Updated Comment");

        doThrow(new QnaCommentException(ErrorCode.COMMENT_NOT_FOUND))
                .when(qnaCommentService).updateQnaComment(any(QnaCommentUpdate.class));

        // when & then
        mockMvc.perform(put("/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("댓글 삭제 실패 테스트 - 존재하지 않는 댓글")
    void deleteQnaComment_NotFound() throws Exception {
        // given
        int commentId = 999;
        doThrow(new QnaCommentException(ErrorCode.COMMENT_NOT_FOUND))
                .when(qnaCommentService).deleteQnaComment(commentId);

        // when & then
        mockMvc.perform(delete("/admin/qna/{id}", commentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("유효하지 않은 요청값 테스트")
    void invalidRequestBody() throws Exception {
        // given
        String invalidJson = "{\"id\":1,}"; // 잘못된 JSON 형식

        // when & then
        mockMvc.perform(put("/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}