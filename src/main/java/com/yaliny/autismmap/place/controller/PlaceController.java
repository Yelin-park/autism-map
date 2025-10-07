package com.yaliny.autismmap.place.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.global.security.CustomUserDetails;
import com.yaliny.autismmap.place.dto.request.PlaceCreateRequest;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import com.yaliny.autismmap.place.dto.response.PlaceDetailResponse;
import com.yaliny.autismmap.place.dto.response.PlaceListResponse;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import com.yaliny.autismmap.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장소 관리 기능")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @Operation(summary = "장소 등록", description = "위도, 경도 변환: https://deveapp.com/map.php")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<String>> registerPlace(@ModelAttribute PlaceCreateRequest form) {
        Long placeId = placeService.registerPlace(form);
        return ResponseEntity.ok(BaseResponse.success("placeId: " + placeId + " 장소 등록 성공"));
    }

    @Operation(summary = "장소 수정", description = "위도, 경도 변환: https://deveapp.com/map.php")
    @PatchMapping(path = "/{placeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<PlaceDetailResponse>> updatePlace(
        @PathVariable Long placeId,
        @ModelAttribute PlaceUpdateRequest form
    ) {
        PlaceDetailResponse response = placeService.updatePlace(placeId, form);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "장소 삭제")
    @DeleteMapping("/{placeId}")
    public ResponseEntity<BaseResponse<String>> deletePlace(@PathVariable Long placeId) {
        placeService.deletePlace(placeId);
        return ResponseEntity.ok(BaseResponse.success("placeId: " + placeId + "장소 삭제 성공"));
    }

    @Operation(summary = "장소 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<PlaceListResponse>> getPlaceList(
        @Parameter(description = "행정 구역 ID")
        @RequestParam(required = false)
        Long provinceId,
        @Parameter(description = "시/군/구 ID")
        @RequestParam(required = false)
        Long districtId,
        @Parameter(description = "카테고리 구분")
        @RequestParam(required = false)
        PlaceCategory category,
        @Parameter(description = "조용한 환경 여부")
        @RequestParam(required = false)
        Boolean isQuiet,
        @Parameter(description = "주차장 유무")
        @RequestParam(required = false)
        Boolean hasParking,
        @Parameter(description = "쉴 수 있는 공간 여부")
        @RequestParam(required = false)
        Boolean hasRestArea,
        @Parameter(description = "프라이빗 룸 여부")
        @RequestParam(required = false)
        Boolean hasPrivateRoom,
        @Parameter(description = "밝기 수준")
        @RequestParam(required = false)
        LightingLevel lightingLevel,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaceListRequest request = PlaceListRequest.of(provinceId, districtId, category, isQuiet, hasParking, hasRestArea, hasPrivateRoom, lightingLevel);
        PlaceListResponse response = placeService.getPlaceList(request, PageRequest.of(page, size), user == null ? null : user.getMemberId());
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "장소 상세 조회")
    @GetMapping("/{placeId}")
    public ResponseEntity<BaseResponse<PlaceDetailResponse>> getPlaceDetail(@PathVariable Long placeId) {
        PlaceDetailResponse response = placeService.getPlaceDetail(placeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

}
