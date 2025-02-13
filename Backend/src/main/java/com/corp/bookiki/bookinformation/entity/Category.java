package com.corp.bookiki.bookinformation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    COMPUTER_SCIENCE(0, "컴퓨터과학"),
    PHILOSOPHY_PSYCHOLOGY(1, "철학/심리학"),
    RELIGION(2, "종교"),
    SOCIAL_SCIENCE(3, "사회과학"),
    LANGUAGE(4, "언어"),
    SCIENCE(5, "과학"),
    TECHNOLOGY(6, "기술"),
    ART(7, "예술"),
    LITERATURE(8, "문학"),
    HISTORY_GEOGRAPHY(9, "역사/지리");

    private final Integer code;
    private final String categoryName;

    // 숫자 -> enum
    public static Category ofCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (Category category : Category.values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category code: " + code);
    }

    // enum -> 숫자
    public static Integer toCode(Category category) {
        if (category == null) {
            return null;
        }
        return category.getCode();
    }
}
