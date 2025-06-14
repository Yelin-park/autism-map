package com.yaliny.autismmap.place.repository;

import com.yaliny.autismmap.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long>, PlaceRepositoryCustom {
}
