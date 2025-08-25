package com.yaliny.autismmap.favorite.service;

import com.yaliny.autismmap.favorite.dto.request.FavoriteListRequest;
import com.yaliny.autismmap.favorite.dto.response.FavoriteDetailResponse;
import com.yaliny.autismmap.favorite.dto.response.FavoriteListResponse;
import com.yaliny.autismmap.favorite.entity.Favorite;
import com.yaliny.autismmap.favorite.repository.FavoriteRepository;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yaliny.autismmap.global.exception.ErrorCode.*;

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

    @Transactional
    public void deleteFavorite(Long memberId, Long favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId).orElseThrow(() -> new CustomException(FAVORITE_NOT_FOUND));

        if (!favorite.getMember().getId().equals(memberId)) {
            throw new CustomException(ACCESS_DENIED);
        }

        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public FavoriteListResponse getFavoriteList(Long memberId, FavoriteListRequest request, PageRequest pageRequest) {
        Page<Favorite> favorites = favoriteRepository.searchFavoritePlace(memberId, request, pageRequest);
        return FavoriteListResponse.of(favorites);
    }

    @Transactional(readOnly = true)
    public FavoriteDetailResponse getFavoriteDetail(Long memberId, Long favoriteId) {
        Favorite favorite = favoriteRepository.findByIdAndMemberId(favoriteId, memberId).orElseThrow(() -> new CustomException(FAVORITE_NOT_FOUND));

        if (!favorite.getMember().getId().equals(memberId)) {
            throw new CustomException(ACCESS_DENIED);
        }

        return FavoriteDetailResponse.of(favorite);
    }
}
