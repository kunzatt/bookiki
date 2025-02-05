package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.resolver.CurrentUserArgumentResolver;
import com.corp.bookiki.qna.controller.QnaController;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.dto.QnaUpdate;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.service.QnaService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({QnaController.class})
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("문의사항 컨트롤러 테스트")
@Slf4j
class QnaControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @MockBean
    private QnaService qnaService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CurrentUserArgumentResolver currentUserArgumentResolver;

    private AuthUser authUser;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        log.info("MockMvc 설정이 완료되었습니다.");

        authUser = AuthUser.builder()
                .id(1)
                .email("test@test.com")
                .role(Role.USER)
                .build();

        when(currentUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(authUser);
    }

    @Test
    @WithMockUser
    @DisplayName("문의사항 등록 성공")
    void createQna_Success() throws Exception {
        // given
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("title", "테스트 제목");
        requestMap.put("qnaType", "일반문의");
        requestMap.put("content", "테스트 내용");

        int expectedId = 1;
        when(qnaService.createQna(any(QnaRequest.class), eq(1))).thenReturn(expectedId);

        // when & then
        mockMvc.perform(post("/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string(String.valueOf(expectedId)));

        verify(qnaService).createQna(any(QnaRequest.class), eq(1));
    }

    @Test
    @WithMockUser
    @DisplayName("문의사항 삭제 성공")
    void deleteQna_Success() throws Exception {
        // given
        int qnaId = 1;
        doNothing().when(qnaService).deleteQna(eq(qnaId), eq(1));

        // when & then
        mockMvc.perform(delete("/qna/{id}", qnaId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(qnaService).deleteQna(eq(qnaId), eq(1));
    }

    @Test
    @WithMockUser
    @DisplayName("문의사항 수정 성공")
    void updateQna_Success() throws Exception {
        // given
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("id", 1);
        requestMap.put("title", "수정된 제목");
        requestMap.put("qnaType", "일반문의");
        requestMap.put("content", "수정된 내용");

        doNothing().when(qnaService).updateQna(any(QnaUpdate.class), eq(1));

        // when & then
        mockMvc.perform(put("/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(qnaService).updateQna(any(QnaUpdate.class), eq(1));
    }

    @Test
    @WithMockUser
    @DisplayName("문의사항 목록 조회 성공")
    void selectQnas_Success() throws Exception {
        // given
        List<QnaEntity> qnaList = new ArrayList<>();
        QnaEntity qna = QnaEntity.builder()
                .title("테스트 제목")
                .qnaType("일반문의")
                .content("테스트 내용")
                .authorId(1)
                .build();
        qnaList.add(qna);

        UserEntity testUser = UserEntity.builder()
                .userName("테스트유저")
                .email("test@test.com")
                .role(Role.USER)
                .build();

        ReflectionTestUtils.setField(testUser, "id", 1);

        when(qnaService.selectQnas(null, null, null, authUser)).thenReturn(qnaList);
        when(userRepository.getReferenceById(1)).thenReturn(testUser);

        // when & then
        mockMvc.perform(get("/qna")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());

        verify(qnaService).selectQnas(null, null, null, authUser);
        verify(userRepository).getReferenceById(1);
    }
}