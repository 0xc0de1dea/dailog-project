package com.example.dailog.domain.comment.service;

import com.example.dailog.common.exception.ErrorCode;
import com.example.dailog.common.exception.ServiceException;
import com.example.dailog.domain.comment.dto.CommentDto;
import com.example.dailog.domain.comment.entity.Comment;
import com.example.dailog.domain.comment.entity.CommentLike;
import com.example.dailog.domain.comment.repository.CommentLikeRepository;
import com.example.dailog.domain.comment.repository.CommentRepository;
import com.example.dailog.domain.daily.entity.Daily;
import com.example.dailog.domain.daily.repository.DailyRepository;
import com.example.dailog.domain.user.entity.User;
import com.example.dailog.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DailyRepository dailyRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Transactional
    public CommentDto.Response create(org.springframework.security.core.userdetails.User user, Long id, CommentDto.Request request) {
        String email = user.getUsername();

        Daily daily = dailyRepository.findById(id)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.DAILY_NOT_FOUND)
                );

        long count = commentRepository.countByDailyId(id);

        if (count >= 10) {
            throw new ServiceException(ErrorCode.EXCEEDED_COMMENT);
        }

        Comment comment = commentRepository.saveAndFlush(
                new Comment(
                        request.getContent(),
                        email,
                        passwordEncoder.encode(request.getPassword()),
                        daily
                )
        );

        return CommentDto.Response.build(
                comment.getId(),
                daily.getId(),
                comment.getContent(),
                comment.getAuthor(),
                0L,
                comment.getCreatedAt(),
                null
        );
    }

    @Transactional(readOnly = true)
    public List<CommentDto.Response> findAll(Long id) {
        Daily daily = dailyRepository.findById(id)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.DAILY_NOT_FOUND)
                );

        List<Comment> comments = daily.getComments();

        return comments
                .stream()
                .map(comment -> CommentDto.Response.build(
                        comment.getId(),
                        comment.getDaily().getId(),
                        comment.getContent(),
                        comment.getAuthor(),
                        comment.getLikes(),
                        comment.getCreatedAt(),
                        comment.getModifiedAt()
                ))
                .toList();
    }

    @Transactional
    public CommentDto.Response update(org.springframework.security.core.userdetails.User user, Long id, CommentDto.Request request) {
        String email = user.getUsername();

        User savedUser = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        Comment savedComment = commentRepository.findById(id)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.COMMENT_NOT_FOUND)
                );

        if (!savedUser.getEmail().equals(savedComment.getAuthor())) {
            throw new ServiceException(ErrorCode.USER_NOT_MATCHED);
        }

        if (!passwordEncoder.matches(request.getPassword(), savedComment.getPassword())) {
            throw new ServiceException(ErrorCode.INVALID_PASSWORD);
        }

        savedComment.update(request.getContent());

        return CommentDto.Response.build(
                savedComment.getId(),
                savedComment.getDaily().getId(),
                savedComment.getContent(),
                savedComment.getAuthor(),
                savedComment.getLikes(),
                savedComment.getCreatedAt(),
                savedComment.getModifiedAt()
        );
    }

    @Transactional
    public void delete(org.springframework.security.core.userdetails.User user, Long id, CommentDto.Request request) {
        String email = user.getUsername();

        User savedUser = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        Comment savedComment = commentRepository.findById(id)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.COMMENT_NOT_FOUND)
                );

        if (!savedUser.getEmail().equals(savedComment.getAuthor())) {
            throw new ServiceException(ErrorCode.USER_NOT_MATCHED);
        }

        if (!passwordEncoder.matches(request.getPassword(), savedComment.getPassword())) {
            throw new ServiceException(ErrorCode.INVALID_PASSWORD);
        }

        commentRepository.deleteById(id);
    }

    @Transactional
    public void like(org.springframework.security.core.userdetails.User user, Long commentId) {
        String email = user.getUsername();

        User savedUser = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        Comment savedComment = commentRepository.findById(commentId)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.COMMENT_NOT_FOUND)
                );

        if (commentLikeRepository.existsByCommentIdAndUserId(savedComment.getId(), savedUser.getId())) {
            throw new ServiceException(ErrorCode.ALREADY_LIKED);
        }

        CommentLike commentLike = new CommentLike(savedComment, savedUser);

        commentLikeRepository.save(commentLike);

        int retry = 0;

        while (retry < 10) {
            try {
                savedComment.like();
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                retry++;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        throw new IllegalArgumentException("좋아요에 실패하였습니다.");
    }
}
