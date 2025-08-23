package com.yaliny.autismmap.favorite.repository;

import com.yaliny.autismmap.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>, FavoriteRepositoryCustom {

    Optional<Favorite> findByMemberIdAndPlaceId(Long memberId, Long placeId);

    boolean existsByMemberIdAndPlaceId(Long memberId, Long placeId);

    @EntityGraph(attributePaths = "place")
    Page<Favorite> findAllByMemberId(Long memberId, Pageable pageable);

}
