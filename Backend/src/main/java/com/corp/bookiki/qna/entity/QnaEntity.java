package com.corp.bookiki.qna.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.corp.bookiki.user.entity.UserEntity;

@Entity
@Table(name = "qnas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QnaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String qnaType;

    @Column
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserEntity user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL)
    private List<QnaCommentEntity> comments = new ArrayList<>();

    @Builder
    private QnaEntity(String title, String qnaType, String content, UserEntity user) {
        this.title = title;
        this.qnaType = qnaType;
        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
    }

    public void update(String title, String qnaType, String content) {
        this.title = title;
        this.qnaType = qnaType;
        this.content = content;
    }

    public void delete() {
        this.deleted = true;
    }
}
