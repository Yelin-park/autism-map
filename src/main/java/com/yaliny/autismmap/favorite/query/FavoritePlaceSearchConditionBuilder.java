package com.yaliny.autismmap.favorite.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yaliny.autismmap.favorite.dto.request.FavoriteListRequest;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import static com.yaliny.autismmap.place.entity.QPlace.place;

@RequiredArgsConstructor
public class FavoritePlaceSearchConditionBuilder {

    private final FavoriteListRequest request;

    private <T> BooleanExpression extractCondition(T value, Function<T, BooleanExpression> expressionFn) {
        return value != null ? expressionFn.apply(value) : null;
    }

    public BooleanExpression[] build() {
        return new BooleanExpression[]{
            provinceEq(),
            districtEq(),
            categoryEq(),
            isQuietEq(),
            hasParkingEq(),
            hasRestAreaEq(),
            hasPrivateRoomEq(),
            lightingLevelEq()
        };
    }

    private BooleanExpression provinceEq() {
        return extractCondition(request.provinceId(), place.province.id::eq);
    }

    private BooleanExpression districtEq() {
        return extractCondition(request.districtId(), place.district.id::eq);
    }

    private BooleanExpression categoryEq() {
        return extractCondition(request.category(), place.category::eq);
    }

    private BooleanExpression isQuietEq() {
        return extractCondition(request.isQuiet(), place.isQuiet::eq);
    }

    private BooleanExpression hasParkingEq() {
        return extractCondition(request.hasParking(), place.hasParking::eq);
    }

    private BooleanExpression hasRestAreaEq() {
        return extractCondition(request.hasRestArea(), place.hasRestArea::eq);
    }

    private BooleanExpression hasPrivateRoomEq() {
        return extractCondition(request.hasPrivateRoom(), place.hasPrivateRoom::eq);
    }

    private BooleanExpression lightingLevelEq() {
        return extractCondition(request.lightingLevel(), place.lightingLevel::eq);
    }
}
