package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.qna.controller.QnaCommentController;
import com.corp.bookiki.qna.dto.QnaCommentRequest;
import com.corp.bookiki.qna.dto.QnaCommentUpdate;
import com.corp.bookiki.qna.service.QnaCommentService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QnaCommentController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("문의사항 답변 컨트롤러 테스트")
@Slf4j
class QnaCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QnaCommentService qnaCommentService;

    @MockBean
    private CurrentUserArgumentResolver currentUserArgumentResolver;

    private AuthUser adminAuthUser;
    private AuthUser userAuthUser;

    @BeforeEach
    void setup() {
        adminAuthUser = AuthUser.builder()
                .id(1)
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();

        userAuthUser = AuthUser.builder()
                .id(2)
                .email("user@test.com")
                .role(Role.USER)
                .build();

        when(currentUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        log.info("MockMvc 설정이 완료되었습니다.");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})  // roles에 정확한 권한 지정
    @DisplayName("문의사항 답변 등록 성공")
    void createQnaComment_Success() throws Exception {
        // given
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("qnaId", 1);
        requestMap.put("content", "테스트 답변 내용");

        int expectedId = 1;
        when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(adminAuthUser);
        when(qnaCommentService.createQnaComment(any(QnaCommentRequest.class), eq(1))).thenReturn(expectedId);

        // when & then
        mockMvc.perform(post("/api/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string(String.valueOf(expectedId)));

        verify(qnaCommentService).createQnaComment(any(QnaCommentRequest.class), eq(1));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("권한이 없는 사용자의 문의사항 답변 등록 실패")
    void createQnaComment_WithoutAdminRole_Fail() throws Exception {
        // given
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("qnaId", 1);
        requestMap.put("content", "테스트 답변 내용");

        // when & then
        mockMvc.perform(post("/api/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                .andDo(print())
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof AuthorizationDeniedException ||
                                result.getResponse().getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));

        verify(qnaCommentService, never()).createQnaComment(any(QnaCommentRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})  // roles에 정확한 권한 지정
    @DisplayName("문의사항 답변 삭제 성공")
    void deleteQnaComment_Success() throws Exception {
        // given
        int commentId = 1;
        when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(adminAuthUser);
        doNothing().when(qnaCommentService).deleteQnaComment(eq(commentId));

        // when & then
        mockMvc.perform(delete("/api/admin/qna/{id}", commentId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(qnaCommentService).deleteQnaComment(eq(commentId));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})  // roles에 정확한 권한 지정
    @DisplayName("문의사항 답변 수정 성공")
    void updateQnaComment_Success() throws Exception {
        // given
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("id", 1);
        requestMap.put("content", "수정된 답변 내용");

        when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(adminAuthUser);
        doNothing().when(qnaCommentService).updateQnaComment(any(QnaCommentUpdate.class));

        // when & then
        mockMvc.perform(put("/api/admin/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(qnaCommentService).updateQnaComment(any(QnaCommentUpdate.class));
    }
}