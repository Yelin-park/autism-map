package com.yaliny.autismmap.member.dto.response;

import com.yaliny.autismmap.member.entity.Member;

public record MemberInfoResponse(
    Long memberId,
    String email,
    String nickname,
    String role
) {
    public static MemberInfoResponse of(Member member) {
        return new MemberInfoResponse(
            member.getId(),
            member.getEmail(),
            member.getNickname(),
            member.getRole().getDescription()
        );
    }
}
