package com.yaliny.autismmap.region.repository;

import com.yaliny.autismmap.region.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {

    List<District> findAllByProvinceId(Long provinceId);
}
