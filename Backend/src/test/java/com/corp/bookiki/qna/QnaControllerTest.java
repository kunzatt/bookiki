package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.QnaException;
import com.corp.bookiki.qna.controller.QnaController;
import com.corp.bookiki.qna.dto.QnaCommentResponse;
import com.corp.bookiki.qna.dto.QnaDetailResponse;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.dto.QnaUpdate;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.service.QnaService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(QnaController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("문의사항 컨트롤러 테스트")
class QnaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QnaService qnaService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        log.info("Setting up QnaControllerTest...");
    }

    @Test
    @DisplayName("문의사항 등록 성공")
    void createQna() throws Exception {
        // given
        QnaRequest request = new QnaRequest();
        ReflectionTestUtils.setField(request, "title", "도서관 이용 문의");
        ReflectionTestUtils.setField(request, "qnaType", "도서관 이용");
        ReflectionTestUtils.setField(request, "content", "도서관 이용 시간이 어떻게 되나요?");
        given(qnaService.creatQna(any(), eq(QnaTestConstants.TEST_USER_ID))).willReturn(1);

        String content = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                post("/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(1))
                .andDo(print());
        verify(qnaService).creatQna(any(QnaRequest.class), eq(QnaTestConstants.TEST_USER_ID));
    }

    @Test
    @DisplayName("문의사항 등록 실패 - 유효성 검증")
    void createQna_ValidationFail() throws Exception {
        // given
        QnaRequest request = new QnaRequest();
        ReflectionTestUtils.setField(request, "title", "");
        ReflectionTestUtils.setField(request, "qnaType", "");
        ReflectionTestUtils.setField(request, "content", "");

        String content = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                post("/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 입력값입니다"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'title')].reason").value("제목은 필수 입력값입니다."))
                .andExpect(jsonPath("$.errors[?(@.field == 'qnaType')].reason").value("문의사항 유형은 필수 입력값입니다."))
                .andExpect(jsonPath("$.errors[?(@.field == 'content')].reason").value("내용은 필수 입력값입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("문의사항 목록 조회 성공")
    void selectQnas() throws Exception {
        // given
        List<QnaEntity> qnas = Arrays.asList(
                createQnaEntity(1, "Question 1", "GENERAL"),
                createQnaEntity(2, "Question 2", "TECHNICAL")
        );
        given(qnaService.selectQnas(any(), any(), any())).willReturn(qnas);

        // when
        ResultActions result = mockMvc.perform(
                get("/qna")
                        .param("keyword", "test")
                        .param("qnaType", "GENERAL")
                        .param("answered", "true")
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Question 1"))
                .andExpect(jsonPath("$[1].title").value("Question 2"))
                .andDo(print());
    }

    @Test
    @DisplayName("문의사항 상세 조회 성공")
    void selectQnaById() throws Exception {
        // given
        int qnaId = 1;
        QnaDetailResponse response = createQnaDetailResponse(qnaId);
        given(qnaService.selectQnaByIdWithComment(qnaId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(
                get("/qna/{id}", qnaId)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(qnaId))
                .andExpect(jsonPath("$.title").value("Test Question"))
                .andDo(print());
    }

    @Test
    @DisplayName("문의사항 상세 조회 실패 - 존재하지 않는 ID")
    void selectQnaById_NotFound() throws Exception {
        // given
        int qnaId = 999;
        given(qnaService.selectQnaByIdWithComment(qnaId))
                .willThrow(new QnaException(ErrorCode.QNA_NOT_FOUND));

        // when
        ResultActions result = mockMvc.perform(
                get("/qna/{id}", qnaId)
        );

        // then
        result.andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("문의사항 삭제 성공")
    void deleteQna() throws Exception {
        // given
        int qnaId = 1;
        doNothing().when(qnaService).deleteQna(qnaId);

        // when
        ResultActions result = mockMvc.perform(
                delete("/qna/{id}", qnaId)
        );

        // then
        result.andExpect(status().isNoContent())
                .andDo(print());

        verify(qnaService).deleteQna(qnaId);
    }

    @Test
    @DisplayName("문의사항 수정 성공")
    void updateQna() throws Exception {
        // given
        QnaUpdate update = new QnaUpdate();
        ReflectionTestUtils.setField(update, "id", 1);
        ReflectionTestUtils.setField(update, "title", "Updated Title");
        ReflectionTestUtils.setField(update, "qnaType", "TECHNICAL");
        ReflectionTestUtils.setField(update, "content", "Updated Content");

        doNothing().when(qnaService).updateQna(any(QnaUpdate.class));

        // when
        ResultActions result = mockMvc.perform(
                put("/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").value("문의사항이 수정되었습니다."))
                .andDo(print());

        verify(qnaService).updateQna(any(QnaUpdate.class));
    }

    // Helper Methods
    private QnaEntity createQnaEntity(int id, String title, String qnaType) {
        QnaEntity qna = QnaEntity.builder()
                .title(title)
                .content("Test Content")
                .qnaType(qnaType)
                .authorId(QnaTestConstants.TEST_USER_ID)
                .build();
        ReflectionTestUtils.setField(qna, "id", id);
        return qna;
    }

    private QnaDetailResponse createQnaDetailResponse(int id) {
        QnaEntity qna = createQnaEntity(id, "Test Question", "GENERAL");
        List<QnaCommentResponse> comments = Collections.emptyList();
        return new QnaDetailResponse(qna, QnaTestConstants.TEST_USER_NAME, comments);
    }
}