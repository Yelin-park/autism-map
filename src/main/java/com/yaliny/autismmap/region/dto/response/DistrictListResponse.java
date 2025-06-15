package com.yaliny.autismmap.region.dto.response;

import com.yaliny.autismmap.region.entity.District;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record DistrictListResponse(
    @Schema(title = "시/군/구 목록")
    List<DistrictResponse> districtResponseList
) {
    public record DistrictResponse(
        Long id,
        Long provinceId,
        String provinceName,
        String name
    ) {
        public static DistrictResponse of(District district) {
            return new DistrictResponse(
                district.getId(),
                district.getProvince().getId(),
                district.getProvince().getName(),
                district.getName()
            );
        }
    }

    public static DistrictListResponse of(List<District> response) {
        return new DistrictListResponse(
            response.stream().map(DistrictResponse::of).toList()
        );
    }
}
