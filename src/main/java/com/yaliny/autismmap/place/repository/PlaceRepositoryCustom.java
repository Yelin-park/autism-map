package com.yaliny.autismmap.place.repository;

import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import com.yaliny.autismmap.place.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlaceRepositoryCustom {
    Page<Place> searchPlace(PlaceListRequest request, Pageable pageable);
}
