package com.corp.bookiki.shelf.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "shelves",
        indexes = {
                @Index(name = "idx_shelf_row", columnList = "shelf_number, line_number")
        }
)
@Getter
@Setter
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
    public ShelfEntity(Integer id, Integer shelfNumber, Integer lineNumber, Integer category) {
        this.id = id;
        this.shelfNumber = shelfNumber;
        this.lineNumber = lineNumber;
        this.category = category;
    }

}
