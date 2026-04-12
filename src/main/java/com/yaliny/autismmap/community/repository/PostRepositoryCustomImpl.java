package com.yaliny.autismmap.community.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yaliny.autismmap.community.entity.CategoryType;
import com.yaliny.autismmap.community.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.yaliny.autismmap.community.entity.QPost.post;
import static com.yaliny.autismmap.member.entity.QMember.member;

@RequiredArgsConstructor
@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> searchPost(CategoryType categoryType, String searchText, Pageable pageable) {
        BooleanExpression searchCondition = searchTextContains(searchText);

        List<Post> content = queryFactory
            .selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(searchCondition, categoryTypeEq(categoryType))
            .orderBy(post.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(post.count())
            .from(post)
            .where(searchCondition, categoryTypeEq(categoryType))
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression searchTextContains(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return null;
        }

        return post.title.contains(searchText)
            .or(post.content.contains(searchText))
            .or(post.member.nickname.contains(searchText));
    }

    private BooleanExpression categoryTypeEq(CategoryType categoryType) {
        return categoryType != null ? post.category.eq(categoryType) : null;
    }
}
