package com.yaliny.autismmap.place.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.place.dto.request.PlaceCreateRequest;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import com.yaliny.autismmap.place.dto.response.PlaceDetailResponse;
import com.yaliny.autismmap.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장소 관리 기능")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "장소 등록")
    @PostMapping
    public ResponseEntity<BaseResponse<String>> registerPlace(@RequestBody PlaceCreateRequest request) {
        Long placeId = placeService.createPlace(request);
        return ResponseEntity.ok(BaseResponse.success("placeId: " + placeId + " 장소 등록 성공"));
    }

    @Operation(summary = "장소 수정")
    @PatchMapping("/{placeId}")
    public ResponseEntity<BaseResponse<PlaceDetailResponse>> updatePlace(
        @PathVariable Long placeId,
        @RequestBody PlaceUpdateRequest request
    ) {
        PlaceDetailResponse response = placeService.updatePlace(placeId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

}
