package com.yaliny.autismmap.favorite.service;

import com.yaliny.autismmap.favorite.entity.Favorite;
import com.yaliny.autismmap.favorite.repository.FavoriteRepository;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yaliny.autismmap.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.yaliny.autismmap.global.exception.ErrorCode.PLACE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public void addFavorite(Long memberId, Long placeId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Place place = placeRepository.findById(placeId).orElseThrow(() -> new CustomException(PLACE_NOT_FOUND));
        if (favoriteRepository.existsByMemberIdAndPlaceId(memberId, placeId)) return;
        favoriteRepository.save(Favorite.createFavorite(member, place));
    }
}
