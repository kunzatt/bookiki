package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.qna.controller.QnaCommentController;
import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.service.QnaCommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(QnaCommentController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("문의사항 답변 컨트롤러 테스트")
class QnaCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QnaCommentService qnaCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        log.info("Setting up QnaCommentControllerTest...");
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void createComment() throws Exception {
        // given
        QnaCommentRequest request = new QnaCommentRequest();
        ReflectionTestUtils.setField(request, "qnaId", 1);
        ReflectionTestUtils.setField(request, "content", "Test Comment");

        given(qnaCommentService.createQnaComment(any(), eq(QnaTestConstants.TEST_ADMIN_ID)))
                .willReturn(1);

        // when
        ResultActions result = mockMvc.perform(
                post("/qna/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 등록 실패 - 유효성 검증")
    void createComment_ValidationFail() throws Exception {
        // given
        QnaCommentRequest request = new QnaCommentRequest();
        ReflectionTestUtils.setField(request, "qnaId", null);
        ReflectionTestUtils.setField(request, "content", "");

        // when
        ResultActions result = mockMvc.perform(
                post("/qna/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 입력값입니다"))
                .andExpect(jsonPath("$.errors").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment() throws Exception {
        // given
        int commentId = 1;
        doNothing().when(qnaCommentService).deleteQnaComment(commentId);

        // when
        ResultActions result = mockMvc.perform(
                delete("/qna/comment/{id}", commentId)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment() throws Exception {
        // given
        QnaCommentUpdate update = new QnaCommentUpdate();
        ReflectionTestUtils.setField(update, "id", 1);
        ReflectionTestUtils.setField(update, "content", "Updated Content");

        doNothing().when(qnaCommentService).updateQnaComment(any(QnaCommentUpdate.class));

        // when
        ResultActions result = mockMvc.perform(
                put("/qna/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").value("댓글이 수정되었습니다."))
                .andDo(print());
    }
}