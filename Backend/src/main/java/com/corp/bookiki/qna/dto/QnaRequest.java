package com.corp.bookiki.qna.dto;

import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.user.entity.UserEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "문의사항 등록 요청")
public class QnaRequest {
    @Schema(description = "제목", example = "도서관 이용 문의", required = true)
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @Schema(description = "문의사항 유형", example = "도서관 이용", required = true)
    @NotBlank(message = "문의사항 유형은 필수 입력값입니다.")
    private String qnaType;

    @Schema(description = "내용", example = "도서관 이용 시간이 어떻게 되나요?", required = true)
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    public QnaEntity toEntity(UserEntity userEntity) {
        return QnaEntity.builder()
                .title(title)
                .qnaType(qnaType)
                .content(content)
                .user(userEntity)
                .build();
    }
}
