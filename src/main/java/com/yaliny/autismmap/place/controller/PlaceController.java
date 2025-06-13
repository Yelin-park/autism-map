package com.yaliny.autismmap.place.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.place.dto.PlaceCreateRequest;
import com.yaliny.autismmap.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
