package com.example.dailog.domain.daily.repository;

import com.example.dailog.domain.comment.entity.Comment;
import com.example.dailog.domain.daily.dto.DailyDto;
import com.example.dailog.domain.daily.entity.Daily;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example.dailog.domain.comment.entity.QComment.comment;
import static com.example.dailog.domain.daily.entity.QDaily.daily;

@RequiredArgsConstructor
public class DailyCustomRepositoryImpl implements DailyCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Daily findByIdWithQuerydsl(Long id) {
        return queryFactory
                .selectFrom(daily)
                .where(daily.id.eq(id))
                .fetchOne();
    }

    @Override
    public List<Comment> findCommentsByIdQuerydsl(Long id) {
        return queryFactory
                .selectFrom(comment)
                .where(comment.daily.id.eq(id))
                .fetch();
    }

    @Override
    public Page<DailyDto.Response> findAllWithQuerydsl(Pageable pageable) {
        List<DailyDto.Response> content = queryFactory
                .select(
                        Projections.constructor(
                                DailyDto.Response.class,
                                daily.id,
                                daily.title,
                                daily.content,
                                daily.author,
                                daily.likes,
                                daily.createdAt,
                                daily.modifiedAt
                        )
                )
                .from(daily)
                .orderBy(daily.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(daily.count())
                .from(daily)
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total != null ? total : 0L
        );
    }
}
