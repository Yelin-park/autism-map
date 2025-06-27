package com.yaliny.autismmap.place.service;

import com.yaliny.autismmap.global.exception.PlaceNotFoundException;
import com.yaliny.autismmap.global.exception.RegionNotFoundException;
import com.yaliny.autismmap.global.external.service.S3Uploader;
import com.yaliny.autismmap.place.dto.request.PlaceCreateRequest;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import com.yaliny.autismmap.region.entity.District;
import com.yaliny.autismmap.region.entity.Province;
import com.yaliny.autismmap.region.repository.DistrictRepository;
import com.yaliny.autismmap.region.repository.ProvinceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaceServiceUnitTest {

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private ProvinceRepository provinceRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private PlaceService placeService;

    private Province province;
    private District district;
    private Place place;

    @BeforeEach
    void setUp() {
        province = Province.createProvince("서울특별시");
        ReflectionTestUtils.setField(province, "id", 1L);

        district = District.createDistrict("강남구");
        ReflectionTestUtils.setField(district, "id", 1L);

        place = mock(Place.class);
        ReflectionTestUtils.setField(place, "id", 10L);
    }

    @Test
    @DisplayName("장소 등록 성공 - 이미지 업로드 포함")
    void registerPlace_success() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(s3Uploader.upload(mockFile, "place-images")).thenReturn("https://s3.aws.com/place.jpg");

        PlaceCreateRequest request = mock(PlaceCreateRequest.class);
        when(request.provinceId()).thenReturn(province.getId());
        when(request.districtId()).thenReturn(district.getId());
        when(request.images()).thenReturn(List.of(mockFile));
        when(request.name()).thenReturn("장소명");
        when(request.description()).thenReturn("장소에 대한 설명");
        when(request.category()).thenReturn(PlaceCategory.CAFE);
        when(request.address()).thenReturn("서울 강남구");
        when(request.latitude()).thenReturn(37.123);
        when(request.longitude()).thenReturn(127.123);
        when(request.isQuiet()).thenReturn(true);
        when(request.hasParking()).thenReturn(true);
        when(request.hasRestArea()).thenReturn(false);
        when(request.hasPrivateRoom()).thenReturn(false);
        when(request.lightingLevel()).thenReturn(LightingLevel.MODERATE);
        when(request.crowdLevel()).thenReturn(CrowdLevel.NORMAL);
        when(request.businessStartTime()).thenReturn("09:00");
        when(request.businessClosingTime()).thenReturn("18:00");
        when(request.dayOff()).thenReturn("일요일");

        when(provinceRepository.findById(1L)).thenReturn(Optional.of(province));
        when(districtRepository.findById(1L)).thenReturn(Optional.of(district));
        when(placeRepository.save(any())).thenAnswer(invocation -> {
            Place place = invocation.getArgument(0);
            ReflectionTestUtils.setField(place, "id", 100L);
            return place;
        });

        Long savedPlaceId = placeService.registerPlace(request);

        assertThat(savedPlaceId).isEqualTo(100L);
        verify(placeRepository).save(any(Place.class));
    }

    @Test
    @DisplayName("장소 등록 실패 - 잘못된 지역 ID")
    void registerPlace_fail_invalid_region() throws IOException {
        PlaceCreateRequest request = mock(PlaceCreateRequest.class);
        when(request.provinceId()).thenReturn(99L);
        when(provinceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.registerPlace(request))
            .isInstanceOf(RegionNotFoundException.class);
    }

    @Test
    @DisplayName("장소 삭제 성공")
    void deletePlace_success() {
        Place place = mock(Place.class);
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));

        placeService.deletePlace(1L);

        verify(placeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("장소 삭제 실패 - 존재하지 않음")
    void deletePlace_fail_not_found() {
        when(placeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.deletePlace(999L))
            .isInstanceOf(PlaceNotFoundException.class);
    }

    @Test
    @DisplayName("장소 수정 성공")
    void updatePlace_success() throws IOException {
        MultipartFile newImage = mock(MultipartFile.class);
        when(newImage.isEmpty()).thenReturn(false);
        when(s3Uploader.upload(newImage, "place-images")).thenReturn("https://s3.aws.com/new.jpg");

        PlaceUpdateRequest request = mock(PlaceUpdateRequest.class);
        when(request.provinceId()).thenReturn(1L);
        when(request.districtId()).thenReturn(1L);
        when(request.preserveImageIds()).thenReturn(List.of());
        when(request.images()).thenReturn(List.of(newImage));

        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(province));
        when(districtRepository.findById(1L)).thenReturn(Optional.of(district));
        when(place.getName()).thenReturn("장소명");
        when(place.getProvince()).thenReturn(province);
        when(place.getDistrict()).thenReturn(district);
        when(place.getImages()).thenReturn(List.of());
        when(place.getCategory()).thenReturn(PlaceCategory.CAFE);
        when(place.getDescription()).thenReturn("");
        when(place.getAddress()).thenReturn("");
        when(place.getLatitude()).thenReturn(37.123);
        when(place.getLongitude()).thenReturn(127.123);
        when(place.isQuiet()).thenReturn(false);
        when(place.isHasParking()).thenReturn(false);
        when(place.isHasRestArea()).thenReturn(false);
        when(place.isHasPrivateRoom()).thenReturn(false);
        when(place.getLightingLevel()).thenReturn(LightingLevel.MODERATE);
        when(place.getCrowdLevel()).thenReturn(CrowdLevel.NORMAL);
        when(place.getBusinessStartTime()).thenReturn(LocalTime.now());
        when(place.getBusinessClosingTime()).thenReturn(LocalTime.now());
        when(place.getDayOff()).thenReturn("일요일");

        placeService.updatePlace(10L, request);

        verify(place).updatePlace(eq(request), eq(province), eq(district), anyList(), eq(List.of()));
    }

    @Test
    @DisplayName("장소 수정 실패 - 존재하지 않는 장소")
    void updatePlace_fail_not_found() {
        when(placeRepository.findById(99L)).thenReturn(Optional.empty());

        PlaceUpdateRequest request = mock(PlaceUpdateRequest.class);

        assertThatThrownBy(() -> placeService.updatePlace(99L, request))
            .isInstanceOf(PlaceNotFoundException.class);
    }

    @Test
    @DisplayName("장소 상세 조회 성공")
    void getPlaceDetail_success() {
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(place.getName()).thenReturn("장소명");
        when(place.getProvince()).thenReturn(province);
        when(place.getDistrict()).thenReturn(district);
        when(place.getImages()).thenReturn(List.of());
        when(place.getCategory()).thenReturn(PlaceCategory.CAFE);
        when(place.getDescription()).thenReturn("");
        when(place.getAddress()).thenReturn("");
        when(place.getLatitude()).thenReturn(37.123);
        when(place.getLongitude()).thenReturn(127.123);
        when(place.isQuiet()).thenReturn(false);
        when(place.isHasParking()).thenReturn(false);
        when(place.isHasRestArea()).thenReturn(false);
        when(place.isHasPrivateRoom()).thenReturn(false);
        when(place.getLightingLevel()).thenReturn(LightingLevel.MODERATE);
        when(place.getCrowdLevel()).thenReturn(CrowdLevel.NORMAL);
        when(place.getBusinessStartTime()).thenReturn(LocalTime.now());
        when(place.getBusinessClosingTime()).thenReturn(LocalTime.now());
        when(place.getDayOff()).thenReturn("일요일");

        placeService.getPlaceDetail(10L);

        verify(placeRepository).findById(10L);
    }

    @Test
    @DisplayName("장소 상세 조회 실패 - 존재하지 않는 장소")
    void getPlaceDetail_fail_not_found() {
        when(placeRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.getPlaceDetail(100L))
            .isInstanceOf(PlaceNotFoundException.class);
    }
}
