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
@Schema(description = "책장 정보 응답")
public class ShelfResponse {

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
            example = "컴퓨터과학"
    )
    private Category category;

    @Schema(name = "ShelfResponse", description = "책장 정보 응답")
    public static ShelfResponse from(
            @Schema(description = "책장 정보 엔티티")
            ShelfEntity entity
    ) {
        ShelfResponse response = new ShelfResponse();
        response.setId(entity.getId());
        response.setShelfNumber(entity.getShelfNumber());
        response.setLineNumber(entity.getLineNumber());
        response.setCategory(Category.ofCode(entity.getCategory()));
        return response;
    }
}
