package com.example.dailog.domain.comment.entity;

import com.example.dailog.common.entity.BaseEntity;
import com.example.dailog.domain.daily.entity.Daily;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(
                        name = "idx_comment_daily_id",
                        columnList = "daily_id"
                )
        }
)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 100, message = "댓글 내용은 최대 100자까지 입력할 수 있습니다.")
    @Column(nullable = false, length = 100)
    private String content;

    @NotBlank(message = "작성자명은 필수입니다.")
    @Column(nullable = false)
    private String author;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String password;

    @Column(nullable = true)
    private Long likes;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_id", nullable = false)
    private Daily daily;

    public Comment(String content, String author, String password, Daily daily) {
        this.content = content;
        this.author = author;
        this.password = password;
        this.likes = 0L;
        this.daily = daily;
    }

    public void update(String content) {
        this.content = content;
    }

    public void like() {
        this.likes++;
    }
}
