package com.corp.bookiki.notice;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.notice.controller.NoticeController;
import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeUpdate;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.service.NoticeService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(NoticeController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)  // Security filter 비활성화
@DisplayName("공지사항 컨트롤러 테스트")
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeService noticeService;  // NoticeService를 Mock으로 설정

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트 실행 전 설정
    @BeforeEach
    public void setup() {
        log.info("Setting up NoticeControllerTest...");
    }

    // 공지사항 작성 테스트
    @WithMockUser(roles = "ADMIN")  // 관리자 권한으로 테스트
    @Test
    @DisplayName("공지사항 작성 성공")
    public void createNotice() throws Exception {
        // given
        NoticeRequest request = new NoticeRequest("제목", "내용");
        given(noticeService.createNotice(any())).willReturn(1);

        // when
        ResultActions result = mockMvc.perform(
                post("/admin/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(1));
    }

    // 공지사항 목록 조회 테스트
    @Test
    @DisplayName("공지사항 목록 조회 및 검색 테스트")
    public void selectAllNoticesTest() throws Exception {
        // given
        List<NoticeEntity> notices = List.of(
                NoticeEntity.builder()
                        .title("Important Notice")
                        .content("Test Content")
                        .build()
        );

        given(noticeService.selectAllNotices()).willReturn(notices);
        given(noticeService.searchNotice("Important")).willReturn(notices);

        // when - 전체 조회 (페이지네이션 포함)
        mockMvc.perform(get("/notices")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Important Notice"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));

        // when - 키워드 검색 (페이지네이션 포함)
        mockMvc.perform(get("/notices")
                        .param("keyword", "Important")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Important Notice"));
    }

    @DisplayName("공지사항 목록 조회 성공")
    @Test
    public void selectNoticeById() throws Exception {
        // given
        final String url = "/notices/{id}";
        final String title = "title";
        final String content = "content";

        NoticeEntity noticeEntity = NoticeEntity.builder()
                .title(title)
                .content(content)
                .build();

        given(noticeService.selectNoticeById(1)).willReturn(noticeEntity);

        // when
        final ResultActions resultActions = mockMvc.perform(get(url, 1));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));
    }

    @WithMockUser(roles = "ADMIN")
    @DisplayName("공지사항 삭제 성공 - 관리자")
    @Test
    public void deleteNotice() throws Exception {
        // given
        final int id = 1;
        doNothing().when(noticeService).deleteNotice(id);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/admin/notices/{id}", id));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @WithMockUser(roles = "USER")
    @DisplayName("공지사항 삭제 실패 - 일반 사용자")
    @Test
    public void deleteNotice_Unauthorized() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(delete("/admin/notices/{id}", 1));

        // then
        resultActions.andExpect(status().isForbidden());
    }


    @WithMockUser(roles = "ADMIN")
    @DisplayName("공지사항 수정 성공 - 관리자")
    @Test
    public void updateNotice() throws Exception {
        // given
        NoticeUpdate update = NoticeUpdate.builder()
                .id(1)
                .title("newTitle")
                .content("newContent")
                .build();

        doNothing().when(noticeService).updateNotice(any(NoticeUpdate.class));

        // when
        ResultActions resultActions = mockMvc.perform(put("/admin/notices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)));

        // then
        resultActions.andExpect(status().isOk());
    }

    @WithMockUser(roles = "USER")
    @DisplayName("공지사항 수정 실패 - 일반 사용자")
    @Test
    public void updateNotice_Unauthorized() throws Exception {
        // given
        NoticeUpdate update = NoticeUpdate.builder()
                .id(1)
                .title("newTitle")
                .content("newContent")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(put("/admin/notices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)));

        // then
        resultActions.andExpect(status().isForbidden());
    }

}