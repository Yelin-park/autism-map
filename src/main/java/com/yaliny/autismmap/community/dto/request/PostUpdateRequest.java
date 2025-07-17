package com.yaliny.autismmap.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {
    @Schema(required = true, title = "제목", description = "제목", example = "수원 갈만한 식당있나요?")
    private String title;
    @Schema(required = true, title = "내용", description = "내용", example = "수원에 8살 아들과 함께 갈만한 식당있나요??")
    private String content;
    @Schema(title = "유지할 미디어 리스트", description = "유지할 미디어 리스트")
    private List<Long> preserveMediaIds;
    @Schema(title = "미디어 리스트", description = "미디어 리스트")
    private List<PostMediaRequest> mediaList;

    public PostUpdateRequest(String title, String content, List<Long> preserveMediaIds, List<PostMediaRequest> mediaList) {
        this.title = title;
        this.content = content;
        this.preserveMediaIds = preserveMediaIds != null ? preserveMediaIds : new ArrayList<>();
        this.mediaList = mediaList != null ? mediaList : new ArrayList<>();
    }
}
