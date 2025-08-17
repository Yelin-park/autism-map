package com.yaliny.autismmap.favorite.entity;

import com.yaliny.autismmap.global.entity.BaseEntity;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Favorite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    public static Favorite createFavorite(final Member member, final Place place) {
        Favorite favorite = new Favorite();
        favorite.member = member;
        favorite.place = place;
        return favorite;
    }
}
