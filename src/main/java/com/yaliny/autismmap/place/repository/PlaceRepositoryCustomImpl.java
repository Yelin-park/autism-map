package com.yaliny.autismmap.place.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.yaliny.autismmap.place.entity.QPlace.place;

@RequiredArgsConstructor
public class PlaceRepositoryCustomImpl implements PlaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Place> searchPlace(
        PlaceListRequest request, Pageable pageable) {

        List<Place> content = queryFactory
            .selectFrom(place)
            .where(
                regionEq(request.region()),
                cityEq(request.city()),
                categoryEq(request.category()),
                isQuietEq(request.isQuiet()),
                hasParkingEq(request.hasParking()),
                hasRestAreaEq(request.hasRestArea()),
                hasPrivateRoomEq(request.hasPrivateRoom()),
                lightingLevelEq(request.lightingLevel())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(place.count())
            .from(place).where(
                regionEq(request.region()),
                cityEq(request.city()),
                categoryEq(request.category()),
                isQuietEq(request.isQuiet()),
                hasParkingEq(request.hasParking()),
                hasRestAreaEq(request.hasRestArea()),
                hasPrivateRoomEq(request.hasPrivateRoom()),
                lightingLevelEq(request.lightingLevel())
        ).fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression regionEq(String region) {
        return StringUtils.hasText(region) ? place.region.eq(region) : null;
    }

    private BooleanExpression cityEq(String city) {
        return StringUtils.hasText(city) ? place.city.eq(city) : null;
    }

    private BooleanExpression categoryEq(PlaceCategory category) {
        return StringUtils.hasText(String.valueOf(category)) ? place.category.eq(category) : null;
    }

    private BooleanExpression isQuietEq(Boolean isQuiet) {
        return isQuiet != null ? place.isQuiet.eq(isQuiet) : null;
    }

    private BooleanExpression hasParkingEq(Boolean hasParking) {
        return hasParking != null ? place.hasParking.eq(hasParking) : null;
    }

    private BooleanExpression hasRestAreaEq(Boolean hasRestArea) {
        return hasRestArea != null ? place.hasRestArea.eq(hasRestArea) : null;
    }

    private BooleanExpression hasPrivateRoomEq(Boolean hasPrivateRoom) {
        return hasPrivateRoom != null ? place.hasPrivateRoom.eq(hasPrivateRoom) : null;
    }

    private BooleanExpression lightingLevelEq(LightingLevel lightingLevel) {
        return StringUtils.hasText(String.valueOf(lightingLevel)) ? place.lightingLevel.eq(lightingLevel) : null;
    }

}
