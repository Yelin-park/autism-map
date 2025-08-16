package com.yaliny.autismmap.region.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Long id;

    @Setter(AccessLevel.PRIVATE)
    @Column(nullable = false)
    private String name; // 예: "강남구", "수원시", "송파구"

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    // 생성 메서드
    public static District createDistrict(String name) {
        District district = new District();
        district.setName(name);
        return district;
    }
}

