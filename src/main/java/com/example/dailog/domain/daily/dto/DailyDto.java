package com.example.dailog.domain.daily.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class DailyDto {

    @Getter
    @AllArgsConstructor
    public static class Request {

        private String title;

        private String content;

        private String password;
    }

    @Getter
    @AllArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"id", "title", "content", "author", "likes", "createdAt", "modifiedAt"})
    public static class Response {

        protected Long id;

        protected String title;

        protected String content;

        protected String author;

        protected Long likes;

        protected LocalDateTime createdAt;

        protected LocalDateTime modifiedAt;

        public Response() {

        }

        public static Response build(
                Long id,
                String title,
                String content,
                String author,
                Long likes,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt
        ) {
            return Response.builder()
                    .id(id)
                    .title(title)
                    .content(content)
                    .author(author)
                    .likes(likes)
                    .createdAt(createdAt)
                    .modifiedAt(modifiedAt)
                    .build();
        }
    }
}
