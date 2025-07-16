package com.yaliny.autismmap.community.dto.request;

import com.yaliny.autismmap.community.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PostMediaRequest {
    @Schema(title = "미디어 타입", description = "미디어 타입")
    private MediaType mediaType;

    @Schema(title = "미디어 파일 URL", description = "미디어 파일 URL")
    private String url;

    public PostMediaRequest(MediaType mediaType, String url) {
        this.mediaType = mediaType;
        this.url = url;
    }
}
