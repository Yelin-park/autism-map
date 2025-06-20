package com.yaliny.autismmap.place.service;

import com.yaliny.autismmap.global.exception.ImageUploadFailedException;
import com.yaliny.autismmap.global.exception.PlaceNotFoundException;
import com.yaliny.autismmap.global.exception.RegionNotFoundException;
import com.yaliny.autismmap.global.external.service.S3Uploader;
import com.yaliny.autismmap.place.dto.request.PlaceCreateRequest;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import com.yaliny.autismmap.place.dto.response.PlaceDetailResponse;
import com.yaliny.autismmap.place.dto.response.PlaceListResponse;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceImage;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public Long createPlace(PlaceCreateRequest request) {

        Province province = provinceRepository.findById(request.provinceId()).orElseThrow(RegionNotFoundException::new);
        District district = districtRepository.findById(request.districtId()).orElseThrow(RegionNotFoundException::new);

        List<PlaceImage> placeImages = Optional.ofNullable(request.images())
            .orElse(List.of())
            .stream()
            .filter(file -> !file.isEmpty())
            .map(file -> {
                String uploadedUrl = null;
                try {
                    uploadedUrl = s3Uploader.upload(file, "place-images");
                } catch (IOException e) {
                    throw new ImageUploadFailedException();
                }
                return PlaceImage.createPlaceImage(uploadedUrl);
            }).toList();

        Place place = createPlace(request, province, district, placeImages);

        Place savedPlace = placeRepository.save(place);
        return savedPlace.getId();
    }

    private static Place createPlace(PlaceCreateRequest request, Province province, District district, List<PlaceImage> placeImages) {
        return Place.createPlace(
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
            request.dayOff(),
            placeImages.toArray(new PlaceImage[0])
        );
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
