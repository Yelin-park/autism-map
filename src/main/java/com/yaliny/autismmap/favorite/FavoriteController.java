package com.yaliny.autismmap.favorite;

import com.yaliny.autismmap.favorite.dto.AddFavoriteRequest;
import com.yaliny.autismmap.favorite.entity.Favorite;
import com.yaliny.autismmap.favorite.service.FavoriteService;
import com.yaliny.autismmap.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "즐겨찾기 기능")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "장소 즐겨찾기 등록")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> addFavorite(@RequestBody AddFavoriteRequest request) {
        favoriteService.addFavorite(request.memberId(), request.memberId());
        return ResponseEntity.ok(BaseResponse.success());
    }
}
