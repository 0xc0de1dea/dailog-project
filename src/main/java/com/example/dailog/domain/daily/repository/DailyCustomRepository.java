package com.example.dailog.domain.daily.repository;

import com.example.dailog.domain.comment.entity.Comment;
import com.example.dailog.domain.daily.dto.DailyDto;
import com.example.dailog.domain.daily.entity.Daily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DailyCustomRepository {

    Daily findByIdWithQuerydsl(Long id);

    List<Comment> findCommentsByIdQuerydsl(Long id);

    Page<DailyDto.Response> findAllWithQuerydsl(Pageable pageable);
}
