package com.yaliny.autismmap.member.repository;

import com.yaliny.autismmap.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(long id);

    boolean existsByNickname(String nickname);

    default void softDeleteByMemberId(Long memberId) {
        findById(memberId).ifPresent(this::delete);
    };
}
