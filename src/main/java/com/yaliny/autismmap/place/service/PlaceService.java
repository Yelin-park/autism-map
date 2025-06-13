package com.yaliny.autismmap.place.service;

import com.yaliny.autismmap.place.dto.PlaceCreateRequest;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;

    @Transactional
    public Long createPlace(PlaceCreateRequest request) {
        Place place = new Place(
            request.name(),
            request.description(),
            request.category(),
            request.address(),
            request.latitude(),
            request.longitude(),
            request.isWheelchairFriendly(),
            request.isQuiet(),
            request.hasParking(),
            request.hasRestArea(),
            request.hasPrivateRoom(),
            request.lightingLevel(),
            request.crowdLevel(),
            request.businessStartTime(),
            request.businessClosingTime(),
            request.dayOff()
        );

        Place savedPlace = placeRepository.save(place);
        return savedPlace.getId();
    }
}
