package com.yaliny.autismmap.place.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import static com.yaliny.autismmap.place.entity.QPlace.place;

@RequiredArgsConstructor
public class PlaceSearchConditionBuilder {

    private final PlaceListRequest request;

    public BooleanExpression[] build() {
        return new BooleanExpression[]{
            regionEq(),
            cityEq(),
            categoryEq(),
            isQuietEq(),
            hasParkingEq(),
            hasRestAreaEq(),
            hasPrivateRoomEq(),
            lightingLevelEq()
        };
    }

    private BooleanExpression regionEq() {
        return StringUtils.hasText(request.region()) ? place.region.eq(request.region()) : null;
    }

    private BooleanExpression cityEq() {
        return StringUtils.hasText(request.city()) ? place.city.eq(request.city()) : null;
    }

    private BooleanExpression categoryEq() {
        return request.category() != null ? place.category.eq(request.category()) : null;
    }

    private BooleanExpression isQuietEq() {
        return request.isQuiet() != null ? place.isQuiet.eq(request.isQuiet()) : null;
    }

    private BooleanExpression hasParkingEq() {
        return request.hasParking() != null ? place.hasParking.eq(request.hasParking()) : null;
    }

    private BooleanExpression hasRestAreaEq() {
        return request.hasRestArea() != null ? place.hasRestArea.eq(request.hasRestArea()) : null;
    }

    private BooleanExpression hasPrivateRoomEq() {
        return request.hasPrivateRoom() != null ? place.hasPrivateRoom.eq(request.hasPrivateRoom()) : null;
    }

    private BooleanExpression lightingLevelEq() {
        return request.lightingLevel() != null ? place.lightingLevel.eq(request.lightingLevel()) : null;
    }
}
