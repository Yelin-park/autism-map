package com.yaliny.autismmap.place.entity;

import com.yaliny.autismmap.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter(AccessLevel.PRIVATE)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_image_id")
    Long id;

    @Lob
    @Column(nullable = false)
    String url;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    public static PlaceImage createPlaceImage(String url) {
        PlaceImage placeImage = new PlaceImage();
        placeImage.setUrl(url);
        return placeImage;
    }
}
