package com.corp.bookiki.qna;

import com.corp.bookiki.global.config.SecurityConfig;
import com.corp.bookiki.global.config.TestSecurityBeansConfig;
import com.corp.bookiki.qna.controller.QnaCommentController;
import com.corp.bookiki.util.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

@WebMvcTest(QnaCommentController.class)
@Import({SecurityConfig.class, CookieUtil.class, TestSecurityBeansConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
@DisplayName("문의사항 답변 컨트롤러 테스트")
@Slf4j
class QnaCommentControllerTest {

}
