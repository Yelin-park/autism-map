package com.yaliny.autismmap.community.dto.request;

public record PostCommentCreateRequest(
    long memberId,
    String content,
    Long parentCommentId
) {
}
