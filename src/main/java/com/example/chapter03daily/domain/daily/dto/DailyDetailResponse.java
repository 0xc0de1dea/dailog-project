package com.example.chapter03daily.domain.daily.dto;

import com.example.chapter03daily.domain.comment.dto.CommentDto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "title", "content", "author", "likes", "createdAt", "modifiedAt", "comments"})
public class DailyDetailResponse extends DailyDto.Response {

    private List<CommentDto.Response> comments;

    @Builder(access = AccessLevel.PRIVATE)
    private DailyDetailResponse(
            Long id,
            String title,
            String content,
            String author,
            Long likes,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            List<CommentDto.Response> comments
    ) {
        super(id, title, content, author, likes, createdAt, modifiedAt);
        this.comments = comments;
    }

    @JsonCreator
    public static DailyDetailResponse build(
            @JsonProperty("id") Long id,
            @JsonProperty("title") String title,
            @JsonProperty("content") String content,
            @JsonProperty("author") String author,
            @JsonProperty("likes") Long likes,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("modifiedAt") LocalDateTime modifiedAt,
            @JsonProperty("comments") List<CommentDto.Response> comments
    ) {
        return DailyDetailResponse.builder()
                .id(id)
                .title(title)
                .content(content)
                .author(author)
                .likes(likes)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .comments(comments)
                .build();
    }
}
