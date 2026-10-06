package com.example.chapter03daily.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * Validation
     */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청값 검증에 실패했습니다."),

    /**
     * User
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 유저를 찾을 수 없습니다."),
    USER_NOT_MATCHED(HttpStatus.CONFLICT, "수정하려는 유저와 일치하지 않습니다."),

    /**
     * Comment
     */
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 댓글을 찾을 수 없습니다."),
    EXCEEDED_COMMENT(HttpStatus.BAD_REQUEST, "하나의 일정에는 댓글을 10개까지만 작성할 수 있습니다."),

    /**
     * ChatRoom
     */
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 채팅방을 찾을 수 없습니다."),

    /**
     * Like
     */
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 누르셨습니다."),

    /**
     * Daily
     */
    DAILY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 일정을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
