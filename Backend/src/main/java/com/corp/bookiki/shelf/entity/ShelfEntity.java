package com.corp.bookiki.shelf.entity;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.ShelfException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "shelves",
        indexes = {
                @Index(name = "idx_shelf_row", columnList = "shelf_number, line_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ShelfEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "shelf_number", nullable = false)
    private Integer shelfNumber;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column
    private Integer category;

    @Builder
    public ShelfEntity(Integer shelfNumber, Integer lineNumber, Integer category) {
        this.shelfNumber = shelfNumber;
        this.lineNumber = lineNumber;
        this.category = category;
    }

    public void update(int shelfNumber, int lineNumber, Integer category) {
        validateShelfNumber(shelfNumber);
        validateLineNumber(lineNumber);

        this.shelfNumber = shelfNumber;
        this.lineNumber = lineNumber;
        this.category = category;
    }

    private void validateLineNumber(int lineNumber) {
        if (shelfNumber <= 0) {
            throw new ShelfException(ErrorCode.INVALID_SHELF_NUMBER);
        }
    }

    private void validateShelfNumber(int shelfNumber) {
        if (lineNumber <= 0) {
            throw new ShelfException(ErrorCode.INVALID_LINE_NUMBER);
        }
    }

}
