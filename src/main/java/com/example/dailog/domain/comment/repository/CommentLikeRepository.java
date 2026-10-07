package com.example.dailog.domain.comment.repository;

import com.example.dailog.domain.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentIdAndUserId(Long comment, Long user);

    long countByCommentId(Long commentId);
}
