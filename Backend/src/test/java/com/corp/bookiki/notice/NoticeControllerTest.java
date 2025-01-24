package com.corp.bookiki.notice;


import com.corp.bookiki.notice.dto.NoticeRequest;
import com.corp.bookiki.notice.dto.NoticeUpdate;
import com.corp.bookiki.notice.entity.NoticeEntity;
import com.corp.bookiki.notice.repository.NoticeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class NoticeControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper; // 직렬화, 역직렬화를 위한 클래스

    @Autowired
    private WebApplicationContext context;

    @Autowired
    NoticeRepository noticeRepository;

    // 테스트 실행 정
    @BeforeEach
    public void mockMvcSetup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        noticeRepository.deleteAll();
    }

    // 공지사항 작성 테스트
    @DisplayName("공지사항 작성 성공")
    @Test
    public void createNotice() throws Exception {
        // given
        final String url = "/admin/notices";
        final String title = "title";
        final String content = "content";
        final NoticeRequest noticeRequest = new NoticeRequest(title, content);

        // 객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(noticeRequest);

        // when
        // 설정한 내용을 바탕으로 요청 전송
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());

        List<NoticeEntity> createdNotices = noticeRepository.findAll();

        assertThat(createdNotices.size()).isEqualTo(1); // 크기가 1인지
        assertThat(createdNotices.get(0).getTitle()).isEqualTo(title);
        assertThat(createdNotices.get(0).getContent()).isEqualTo(content);

    }

    // 공지사항 목록 조회 테스트
    @Test
    @DisplayName("공지사항 목록 조회 및 검색 테스트")
    public void selectAllNoticesTest() throws Exception {
        // given
        NoticeEntity notice = NoticeEntity.builder()
                .title("Important Notice")
                .content("Test Content")
                .build();
        noticeRepository.save(notice);

        // when - 전체 조회
        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Important Notice"));

        // when - 키워드 검색
        mockMvc.perform(get("/notices")
                        .param("keyword", "Important"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Important Notice"));
    }

    @DisplayName("공지사항 목록 조회 성공")
    @Test
    public void selectNoticeById() throws Exception {
        // given
        final String url = "/notices/{id}";
        final String title = "title";
        final String content = "content";

        NoticeEntity selectedNotice = noticeRepository.save(NoticeEntity.builder()
                .title(title)
                .content(content)
                .build());

        // when
        final ResultActions resultActions = mockMvc.perform(get(url, selectedNotice.getId()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));
    }

    @DisplayName("공지사항 삭제 성공")
    @Test
    public void deleteNotice() throws Exception {
        // given
        final String url = "/admin/notices/{id}";
        final String title = "title";
        final String content = "content";

        NoticeEntity deletedNotice = noticeRepository.save(NoticeEntity.builder()
                .title(title)
                .content(content)
                .build());

        // when
        ResultActions resultActions = mockMvc.perform(delete(url, deletedNotice.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNoContent());
        NoticeEntity noticeEntity = noticeRepository.findById(deletedNotice.getId()).get();
        assertThat(noticeEntity.isDeleted()).isTrue();
    }

    @DisplayName("공지사항 수정 성공")
    @Test
    public void updateArticle() throws Exception {
        // given
        final String url = "/admin/notices";
        final String title = "title";
        final String content = "content";

        NoticeEntity beforeUpdate = noticeRepository.save(NoticeEntity.builder()
                .title(title)
                .content(content)
                .build());

        final String newTitle = "newTitle";
        final String newContent = "newContent";

        NoticeUpdate update = NoticeUpdate.builder()
                .id(beforeUpdate.getId())
                .title(newTitle)
                .content(newContent)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(update)));

        // then
        resultActions.andExpect(status().isOk());

        NoticeEntity noticeEntity = noticeRepository.findById(beforeUpdate.getId()).get();

        assertThat(noticeEntity.getTitle()).isEqualTo(newTitle);
        assertThat(noticeEntity.getContent()).isEqualTo(newContent);
    }

}