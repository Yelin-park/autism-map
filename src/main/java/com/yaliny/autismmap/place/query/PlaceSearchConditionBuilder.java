package com.yaliny.autismmap.place.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import lombok.RequiredArgsConstructor;

import static com.yaliny.autismmap.place.entity.QPlace.place;

@RequiredArgsConstructor
public class PlaceSearchConditionBuilder {

    private final PlaceListRequest request;

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
        return request.provinceId() != null ? place.province.id.eq(request.provinceId()) : null;
    }

    private BooleanExpression districtEq() {
        return request.districtId() != null ? place.district.id.eq(request.districtId()) : null;
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
