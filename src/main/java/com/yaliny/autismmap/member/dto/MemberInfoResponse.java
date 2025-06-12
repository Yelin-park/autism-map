package com.yaliny.autismmap.member.dto;

public record MemberInfoResponse(
    Long memberId,
    String email,
    String nickname
) {
}
