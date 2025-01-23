package com.corp.bookiki.notice;

import com.corp.bookiki.notice.controller.NoticeController;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeResponse;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.service.NoticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeController.class)
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("공지사항 컨트롤러 테스트")
@Slf4j
class NoticeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeService noticeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createNotice_WhenValidRequest_ReturnsCreated() throws Exception {
        // given
        NoticeRequest request = new NoticeRequest();
        request.setTitle("Test Notice");
        request.setContent("Test Content");
        log.info("테스트 요청 생성: {}", request.getTitle());

        // when & then
        mockMvc.perform(post("/admin/notices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("공지사항이 등록되었습니다."));
    }

    @Test
    @WithMockUser
    void getNotices_ReturnsNoticeList() throws Exception {
        // given
        List<NoticeResponse> notices = Arrays.asList(
                NoticeResponse.builder()
                        .noticeEntity(NoticeEntity.builder()
                                .title("Notice 1")
                                .content("Content 1")
                                .build())
                        .build()
        );

        given(noticeService.findAllNotices())
                .willReturn(notices.stream()
                        .map(response -> NoticeEntity.builder()
                                .title(response.getTitle())
                                .content(response.getContent())
                                .build())
                        .collect(Collectors.toList()));
        log.info("Mock 서비스 설정 완료");

        // when & then
        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Notice 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNotice_ReturnsNoContent() throws Exception {
        // given
        int noticeId = 1;
        log.info("공지사항 삭제 테스트: id={}", noticeId);

        // when & then
        mockMvc.perform(delete("/admin/notices/{id}", noticeId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}