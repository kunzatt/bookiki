package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.JacksonConfig;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.global.config.WebMvcConfig;
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
import com.corp.bookiki.user.service.UserService;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({QnaController.class})
@Import({WebMvcConfig.class, CurrentUserArgumentResolver.class, TestSecurityBeansConfig.class, SecurityConfig.class, CookieUtil.class})
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
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

	private String testEmail;
    private Authentication auth;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        log.info("MockMvc 설정이 완료되었습니다.");

        testEmail = "test@example.com";
		UserEntity testUserEntity = UserEntity.builder()
			.email(testEmail)
			.userName("Test User")
			.role(Role.USER)
			.build();
        ReflectionTestUtils.setField(testUserEntity, "id", 1);

        given(userRepository.findByEmail(eq(testEmail)))
            .willReturn(Optional.of(testUserEntity));

        auth = new UsernamePasswordAuthenticationToken(
            testEmail,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
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
        mockMvc.perform(post("/api/qna")
                        .with(authentication(auth))
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
        mockMvc.perform(delete("/api/qna/{id}", qnaId)
                    .with(authentication(auth)))
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
        mockMvc.perform(put("/api/qna")
                        .with(authentication(auth))
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
        UserEntity testUser = UserEntity.builder()
            .userName("Test User")
            .email("test@example.com")
            .role(Role.USER)
            .build();
        ReflectionTestUtils.setField(testUser, "id", 1);

        QnaEntity qna = QnaEntity.builder()
            .title("테스트 제목")
            .qnaType("일반문의")
            .content("테스트 내용")
            .user(testUser)
            .build();
        ReflectionTestUtils.setField(qna, "id", 1);

        List<QnaEntity> qnaList = List.of(qna);
        Pageable pageable = PageRequest.of(0, 10);
        Page<QnaEntity> qnaPage = new PageImpl<>(qnaList, pageable, qnaList.size());

        // Mock 설정
        lenient().when(qnaService.selectQnas(
            isNull(),
            isNull(),
            isNull(),
            any(AuthUser.class),  // authUser
            any(Pageable.class)   // pageable
        )).thenReturn(qnaPage);

        lenient().when(userService.getUserById(anyInt())).thenReturn(testUser);

        // when & then
        mockMvc.perform(get("/api/qna")
                .with(authentication(auth))
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content[0].title").value("테스트 제목"))
            .andExpect(jsonPath("$.content[0].authorName").value("Test User"))
            .andExpect(jsonPath("$.pageable").exists())
            .andExpect(jsonPath("$.totalElements").exists());
    }
}