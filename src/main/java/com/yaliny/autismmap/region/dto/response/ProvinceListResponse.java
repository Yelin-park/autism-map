package com.yaliny.autismmap.region.dto.response;

import com.yaliny.autismmap.region.entity.Province;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProvinceListResponse(
    @Schema(title = "행정 구역 목록")
    List<ProvinceResponse> provinceResponseList
) {
    public record ProvinceResponse(
        Long id,
        String name
    ) {
        public static ProvinceResponse of(Province province) {
            return new ProvinceResponse(
                province.getId(),
                province.getName()
            );
        }
    }

    public static ProvinceListResponse of(List<Province> response) {
        return new ProvinceListResponse(
            response.stream().map(ProvinceResponse::of).toList()
        );
    }
}
