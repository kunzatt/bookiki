package com.corp.bookiki.chatbot;

import com.corp.bookiki.chatbot.dto.ChatbotRequest;
import com.corp.bookiki.chatbot.dto.ChatbotResponse;
import com.corp.bookiki.chatbot.service.ChatbotService;
import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ChatbotControllerTest.class})
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("챗봇 컨트롤러 테스트")
@Slf4j
class ChatbotControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatbotService chatbotService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("메시지 전송 성공 테스트")
    void sendMessageSuccess() throws Exception {
        // given
        ChatbotRequest request = ChatbotRequest.builder()
                .message("도서관 이용시간 알려줘")
                .build();

        ChatbotResponse response = ChatbotResponse.builder()
                .message("도서관은 9시부터 6시까지 이용 가능합니다.")
                .intent("library_hours")
                .confidenceScore(0.8f)
                .build();

        AuthUser user = AuthUser.builder()
                .id(1)
                .build();

        given(chatbotService.processMessage(any(), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/chatbot/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도서관은 9시부터 6시까지 이용 가능합니다."))
                .andExpect(jsonPath("$.intent").value("library_hours"))
                .andExpect(jsonPath("$.confidenceScore").value(0.8))
                .andDo(print());
    }

    @Test
    @DisplayName("잘못된 요청 테스트")
    void sendMessageBadRequest() throws Exception {
        // given
        ChatbotRequest request = ChatbotRequest.builder()
                .message("")
                .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/chatbot/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andDo(print());
    }
}