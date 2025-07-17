package com.yaliny.autismmap.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCommentCreateRequest(
    @Schema(title = "사용자 ID", description = "사용자 ID")
    long memberId,
    @Schema(title = "댓글 내용", description = "댓글 내용")
    String content,
    @Schema(title = "대댓글 작성 시 부모 댓글 ID", description = "대댓글 작성 시 부모 댓글 ID")
    Long parentCommentId
) {
}
