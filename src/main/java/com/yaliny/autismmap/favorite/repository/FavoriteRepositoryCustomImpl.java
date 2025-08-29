package com.yaliny.autismmap.favorite.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yaliny.autismmap.favorite.dto.request.FavoriteListRequest;
import com.yaliny.autismmap.favorite.entity.Favorite;
import com.yaliny.autismmap.favorite.query.FavoritePlaceSearchConditionBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.yaliny.autismmap.favorite.entity.QFavorite.favorite;
import static com.yaliny.autismmap.place.entity.QPlace.place;
import static com.yaliny.autismmap.region.entity.QDistrict.district;
import static com.yaliny.autismmap.region.entity.QProvince.province;

@RequiredArgsConstructor
@Repository
public class FavoriteRepositoryCustomImpl implements FavoriteRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Favorite> searchFavoritePlace(Long memberId, FavoriteListRequest request, Pageable pageable) {
        FavoritePlaceSearchConditionBuilder conditionBuilder = new FavoritePlaceSearchConditionBuilder(request);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(favorite.member.id.eq(memberId));
        for (var expr : conditionBuilder.build()) {
            if (expr != null) predicates.add(expr);
        }

        Predicate whereAll = ExpressionUtils.allOf(predicates); // null 무시하고 AND 결합

        List<Favorite> content = queryFactory
            .selectFrom(favorite)
            .join(favorite.place, place).fetchJoin()
            .join(place.province, province).fetchJoin()
            .join(place.district, district).fetchJoin()
            .where(whereAll)
            .orderBy(favorite.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(favorite.count())
            .from(favorite)
            .join(favorite.place, place) // place 기준 조건 평가를 위해 조인만 유지
            .where(whereAll)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
