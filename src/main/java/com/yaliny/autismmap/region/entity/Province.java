package com.yaliny.autismmap.region.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "province_id")
    private Long id;

    @Column(nullable = false)
    private String name; // 예: "서울특별시", "경기도", "부산광역시"

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
    private List<District> districts = new ArrayList<>();

    // 연관 관계 편의 메서드
    public void addDistrict(District district) {
        this.districts.add(district);
        district.setProvince(this);
    }

    // 생성 메서드
    public static Province createProvince(String name, District... districts) {
        Province province = new Province();
        province.setName(name);
        for (District district : districts) {
            province.addDistrict(district);
        }
        return province;
    }

}

