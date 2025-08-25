package com.yaliny.autismmap.favorite.controller;

import com.yaliny.autismmap.favorite.dto.request.AddFavoriteRequest;
import com.yaliny.autismmap.favorite.dto.request.FavoriteListRequest;
import com.yaliny.autismmap.favorite.dto.response.FavoriteDetailResponse;
import com.yaliny.autismmap.favorite.dto.response.FavoriteListResponse;
import com.yaliny.autismmap.favorite.service.FavoriteService;
import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.global.security.CustomUserDetails;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "즐겨찾기 기능")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "장소 즐겨찾기 등록")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> addFavorite(@RequestBody AddFavoriteRequest request) {
        favoriteService.addFavorite(request.memberId(), request.placeId());
        return ResponseEntity.ok(BaseResponse.success());
    }

    @Operation(summary = "장소 즐겨찾기 삭제")
    @DeleteMapping("{favoriteId}")
    public ResponseEntity<BaseResponse<Void>> deleteFavorite(
        @PathVariable Long favoriteId,
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        favoriteService.deleteFavorite(customUserDetails.getMemberId(), favoriteId);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @Operation(summary = "장소 즐겨찾기 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<FavoriteListResponse>> getFavoriteList(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
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
        @RequestParam(defaultValue = "10") int size
    ) {
        FavoriteListRequest request = FavoriteListRequest.of(provinceId, districtId, category, isQuiet, hasParking, hasRestArea, hasPrivateRoom, lightingLevel);
        FavoriteListResponse response = favoriteService.getFavoriteList(customUserDetails.getMemberId(), request, PageRequest.of(page, size));
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "장소 즐겨찾기 상세 조회")
    @GetMapping("{favoriteId}")
    public ResponseEntity<BaseResponse<FavoriteDetailResponse>> getFavoriteDetail(
        @PathVariable Long favoriteId,
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        FavoriteDetailResponse response = favoriteService.getFavoriteDetail(customUserDetails.getMemberId(), favoriteId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
