package com.corp.bookiki.shelf.dto;

import com.corp.bookiki.bookinformation.entity.Category;
import com.corp.bookiki.shelf.entity.ShelfEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "책 정보 수정 요청")
public class ShelfUpdateRequest {

    @Schema(description = "책장 ID", example = "1")
    private Integer id;

    @Schema(
            description = "책장 번호",
            example = "1",
            required = true
    )
    @NotNull(message = "책장 번호는 필수 입력값입니다.")
    private Integer shelfNumber;

    @Schema(
            description = "칸 번호",
            example = "1",
            required = true
    )
    @NotNull(message = "칸 번호는 필수 입력값입니다.")
    private Integer lineNumber;

    @Schema(
            description = "카테고리 이름",
            example = "종교"
    )
    private Category category;

    @Schema(name = "ShelfRequest", description = "책장 정보 요청")
    public ShelfEntity toEntity() {
        return ShelfEntity.builder()
                .shelfNumber(this.shelfNumber)
                .lineNumber(this.lineNumber)
                .category(this.category.getCode())
                .build();
    }
}
