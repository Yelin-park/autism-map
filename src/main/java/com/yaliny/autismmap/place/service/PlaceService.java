package com.yaliny.autismmap.place.service;

import com.yaliny.autismmap.global.exception.PlaceNotFoundException;
import com.yaliny.autismmap.global.exception.RegionNotFoundException;
import com.yaliny.autismmap.place.dto.request.PlaceCreateRequest;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import com.yaliny.autismmap.place.dto.response.PlaceDetailResponse;
import com.yaliny.autismmap.place.dto.response.PlaceListResponse;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import com.yaliny.autismmap.region.entity.District;
import com.yaliny.autismmap.region.entity.Province;
import com.yaliny.autismmap.region.repository.DistrictRepository;
import com.yaliny.autismmap.region.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;

    @Transactional
    public Long createPlace(PlaceCreateRequest request) {

        Province province = provinceRepository.findById(request.provinceId()).orElseThrow(RegionNotFoundException::new);
        District district = districtRepository.findById(request.districtId()).orElseThrow(RegionNotFoundException::new);

        Place place = new Place(
            request.name(),
            request.description(),
            request.category(),
            province,
            district,
            request.address(),
            request.latitude(),
            request.longitude(),
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

    @Transactional
    public PlaceDetailResponse updatePlace(Long placeId, PlaceUpdateRequest request) {
        Place findPlace = placeRepository.findById(placeId).orElseThrow(PlaceNotFoundException::new);
        Province province = provinceRepository.findById(request.provinceId()).orElseThrow(RegionNotFoundException::new);
        District district = districtRepository.findById(request.districtId()).orElseThrow(RegionNotFoundException::new);
        findPlace.updatePlace(request, province, district);
        return PlaceDetailResponse.of(findPlace);
    }

    @Transactional
    public void deletePlace(Long placeId) {
        placeRepository.findById(placeId).orElseThrow(PlaceNotFoundException::new);
        placeRepository.deleteById(placeId);
    }

    @Transactional(readOnly = true)
    public PlaceListResponse getPlaceList(PlaceListRequest request, PageRequest pageRequest) {
        Page<Place> response = placeRepository.searchPlace(request, pageRequest);
        return PlaceListResponse.of(response);
    }

    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeId) {
        Place place = placeRepository.findById(placeId).orElseThrow(PlaceNotFoundException::new);
        return PlaceDetailResponse.of(place);
    }
}
