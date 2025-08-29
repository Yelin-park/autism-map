package com.yaliny.autismmap.favorite.repository;

import com.yaliny.autismmap.favorite.dto.request.FavoriteListRequest;
import com.yaliny.autismmap.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {
    Page<Favorite> searchFavoritePlace(Long memberId, FavoriteListRequest request, Pageable pageable);
}
