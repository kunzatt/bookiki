package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.jwt.JwtTokenProvider;
import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.qna.controller.QnaController;
import com.corp.bookiki.qna.dto.QnaRequest;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.qna.service.QnaService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.CustomUserDetailsService;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(controllers = QnaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SecurityConfig.class, CookieUtil.class})
@ExtendWith(MockitoExtension.class)
class QnaControllerTest {

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QnaService qnaService;

    @MockBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("문의사항 생성 API 테스트")
    void createQna() throws Exception {
        // given
        QnaRequest request = new QnaRequest();
        ReflectionTestUtils.setField(request, "title", "Test Title");
        ReflectionTestUtils.setField(request, "qnaType", "GENERAL");
        ReflectionTestUtils.setField(request, "content", "Test Content");

        when(qnaService.createQna(any(), anyInt())).thenReturn(1);

        // when & then
        MockHttpServletRequestBuilder requestBuilder = post("/qna")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                        securityContext.setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        createTestAuthUser(),
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        );
                        request.getSession().setAttribute(
                                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                securityContext
                        );
                        return request;
                    }
                });

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("문의사항 목록 조회 API 테스트")
    void selectQnas() throws Exception {
        // given
        List<QnaEntity> qnas = Arrays.asList(
                createMockQna(1, "Question 1"),
                createMockQna(2, "Question 2")
        );

        when(qnaService.selectQnas(any(), any(), any(), any()))
                .thenReturn(qnas);

        // when & then
        MockHttpServletRequestBuilder requestBuilder = get("/qna")
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                        securityContext.setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        createTestAuthUser(),
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        );
                        request.getSession().setAttribute(
                                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                securityContext
                        );
                        return request;
                    }
                });

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    private QnaEntity createMockQna(int id, String title) {
        QnaEntity qna = QnaEntity.builder()
                .title(title)
                .content("Test Content")
                .qnaType("GENERAL")
                .authorId(1)
                .build();
        ReflectionTestUtils.setField(qna, "id", id);
        return qna;
    }

    private AuthUser createTestAuthUser() {
        return new AuthUser(1, "test@test.com", Role.USER);
    }
}