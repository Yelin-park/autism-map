package com.yaliny.autismmap.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class PostCreateRequest {
    @Schema(required = true, title = "사용자 ID", description = "사용자 ID")
    private long memberId;

    @Schema(required = true, title = "제목", description = "게시글 제목", example = "수원 갈만한 식당 있나요?")
    private String title;

    @Schema(required = true, title = "내용", description = "게시글 내용", example = "수원에 8살 아들과 함께 갈만한 식당있나요??")
    private String content;

    @Schema(title = "미디어 리스트", description = "미디어 리스트")
    private List<PostMediaRequest> mediaList = new ArrayList<>();

    public PostCreateRequest(long memberId, String title, String content, List<PostMediaRequest> mediaList) {
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.mediaList = mediaList != null ? mediaList : new ArrayList<>();
    }
}
