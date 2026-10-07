package com.example.dailog.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class CommentDto {

    @Getter
    @AllArgsConstructor
    public static class Request {

        private String content;

        private String password;
    }

    @Getter
    @AllArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"commentId", "dailyId", "content", "author", "createdAt", "modifiedAt"})
    public static class Response {

        private Long commentId;

        private Long dailyId;

        private String content;

        private String author;

        private Long likes;

        private LocalDateTime createdAt;

        private LocalDateTime modifiedAt;

        public static Response build(
                Long commentId,
                Long dailyId, String content, String author, Long likes,
                LocalDateTime createdAt, LocalDateTime modifiedAt
        ) {
            return Response.builder()
                    .commentId(commentId)
                    .dailyId(dailyId)
                    .content(content)
                    .author(author)
                    .likes(likes)
                    .createdAt(createdAt)
                    .modifiedAt(modifiedAt)
                    .build();
        }
    }
}
