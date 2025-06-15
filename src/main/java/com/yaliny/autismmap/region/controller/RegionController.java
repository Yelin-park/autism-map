package com.yaliny.autismmap.region.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.region.dto.response.DistrictListResponse;
import com.yaliny.autismmap.region.dto.response.ProvinceListResponse;
import com.yaliny.autismmap.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지역 관리 기능", description = "행정 구역(도/특별시/광역시), 시/군/구를 관리하는 기능입니다.")
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @Operation(summary = "행정 구역 조회")
    @GetMapping("/province")
    public ResponseEntity<BaseResponse<ProvinceListResponse>> getProvinceList() {
        ProvinceListResponse response = regionService.getProvinceList();
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "시/군/구 조회")
    @GetMapping("/district")
    public ResponseEntity<BaseResponse<DistrictListResponse>> getDistrictList(
        @RequestParam Long provinceId
    ) {
        DistrictListResponse response = regionService.getDistrictList(provinceId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

}
