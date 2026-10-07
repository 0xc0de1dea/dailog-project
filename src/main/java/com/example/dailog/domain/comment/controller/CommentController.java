package com.example.dailog.domain.comment.controller;

import com.example.dailog.common.dto.ApiResponse;
import com.example.dailog.domain.comment.dto.CommentDto;
import com.example.dailog.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentDto.Response>> create(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CommentDto.Request request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(commentService.create(user, id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<List<CommentDto.Response>>> findAll(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(commentService.findAll(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentDto.Response>> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CommentDto.Request request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(commentService.update(user, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CommentDto.Request request
    ) {
        commentService.delete(user, id, request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent());
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<ApiResponse<Void>> like(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        commentService.like(user, id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent());
    }
}
