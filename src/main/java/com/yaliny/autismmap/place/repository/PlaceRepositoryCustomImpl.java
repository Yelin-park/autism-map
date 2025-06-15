package com.yaliny.autismmap.place.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.query.PlaceSearchConditionBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.yaliny.autismmap.place.entity.QPlace.place;
import static com.yaliny.autismmap.region.entity.QDistrict.district;
import static com.yaliny.autismmap.region.entity.QProvince.province;

@Repository
public class PlaceRepositoryCustomImpl implements PlaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PlaceRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Place> searchPlace(
        PlaceListRequest request, Pageable pageable) {
        PlaceSearchConditionBuilder conditionBuilder = new PlaceSearchConditionBuilder(request);

        List<Place> content = queryFactory
            .selectFrom(place)
            .join(place.province, province).fetchJoin()
            .join(place.district, district).fetchJoin()
            .where(conditionBuilder.build())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(place.count())
            .from(place)
            .where(conditionBuilder.build())
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

}
