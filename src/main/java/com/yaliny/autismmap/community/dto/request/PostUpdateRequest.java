package com.yaliny.autismmap.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostUpdateRequest(
    @Schema(required = true, title = "제목", description = "제목", example = "수원 갈만한 식당있나요?")
    String title,
    @Schema(required = true, title = "내용", description = "내용", example = "수원에 8살 아들과 함께 갈만한 식당있나요??")
    String content,
    @Schema(title = "유지할 미디어 리스트", description = "유지할 미디어 리스트")
    List<Long> preserveMediaIds,
    @Schema(title = "미디어 리스트", description = "미디어 리스트")
    List<PostMediaRequest> mediaList
) {

}
