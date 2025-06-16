package com.yaliny.autismmap.place.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import static com.yaliny.autismmap.place.entity.QPlace.place;

/**
 * 실제 빌더 패턴 적용하지 않음
 * 전체 조건을 선택적으로 가져와서 사용하지 않아 현재는 build() 호출로 전체 조건을 체크하는 형태로 생성
 */
@RequiredArgsConstructor
public class PlaceSearchConditionBuilder {

    private final PlaceListRequest request;

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
