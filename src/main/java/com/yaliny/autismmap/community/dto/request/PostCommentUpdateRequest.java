package com.yaliny.autismmap.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCommentUpdateRequest(
    @Schema(title = "수정할 댓글 내용", description = "수정할 댓글 내용")
    String content
) {
}
