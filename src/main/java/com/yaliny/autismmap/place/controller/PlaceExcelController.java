package com.yaliny.autismmap.place.controller;

import com.yaliny.autismmap.place.service.PlaceExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "장소 관리 엑셀 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places/admin")
public class PlaceExcelController {

    private final PlaceExcelService placeExcelService;

    @Operation(summary = "장소 엑셀 파일 등록", description = "위도, 경도 변환: https://deveapp.com/map.php")
    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadExcel(
        @RequestPart("file") MultipartFile file
    ) {
        placeExcelService.uploadExcel(file);
        return ResponseEntity.ok("엑셀 업로드 및 저장 완료");
    }
}
