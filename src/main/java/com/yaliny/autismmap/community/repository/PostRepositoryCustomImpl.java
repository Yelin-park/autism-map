package com.yaliny.autismmap.community.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yaliny.autismmap.community.entity.Post;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.yaliny.autismmap.community.entity.QPost.post;
import static com.yaliny.autismmap.member.entity.QMember.member;

@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Post> searchPost(String searchText, Pageable pageable) {
        List<Post> content = queryFactory
            .selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(
                titleContains(searchText)
                    .or(contentContains(searchText))
                    .or(memberNickNameContains(searchText))
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(post.count())
            .from(post)
            .where(
                titleContains(searchText)
                    .or(contentContains(searchText))
                    .or(memberNickNameContains(searchText))
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression titleContains(String searchText) {
        return searchText != null ? post.title.contains(searchText) : null;
    }

    private BooleanExpression contentContains(String searchText) {
        return searchText != null ? post.content.contains(searchText) : null;
    }

    private BooleanExpression memberNickNameContains(String searchText) {
        return searchText != null ? post.member.nickname.contains(searchText) : null;
    }
}
