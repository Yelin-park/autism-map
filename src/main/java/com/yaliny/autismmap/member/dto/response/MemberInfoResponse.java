package com.yaliny.autismmap.member.dto.response;

public record MemberInfoResponse(
    Long memberId,
    String email,
    String nickname
) {
}
