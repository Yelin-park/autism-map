package com.yaliny.autismmap.region.service;

import com.yaliny.autismmap.region.dto.response.DistrictListResponse;
import com.yaliny.autismmap.region.dto.response.ProvinceListResponse;
import com.yaliny.autismmap.region.entity.District;
import com.yaliny.autismmap.region.entity.Province;
import com.yaliny.autismmap.region.repository.DistrictRepository;
import com.yaliny.autismmap.region.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;

    @Transactional(readOnly = true)
    public ProvinceListResponse getProvinceList() {
        List<Province> provinces = provinceRepository.findAll();
        return ProvinceListResponse.of(provinces);
    }

    @Transactional(readOnly = true)
    public DistrictListResponse getDistrictList(Long provinceId) {
        List<District> districtList = districtRepository.findAllByProvinceId(provinceId);
        return DistrictListResponse.of(districtList);
    }
}
