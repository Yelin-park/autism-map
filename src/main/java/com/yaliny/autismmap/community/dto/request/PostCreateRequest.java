package com.yaliny.autismmap.community.dto.request;

import com.yaliny.autismmap.community.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostCreateRequest(
    @Schema(required = true, title = "사용자 ID", description = "사용자 ID")
    long memberId,
    @Schema(required = true, title = "제목", description = "제목", example = "수원 갈만한 식당있나요?")
    String title,
    @Schema(required = true, title = "내용", description = "내용", example = "수원에 8살 아들과 함께 갈만한 식당있나요??")
    String content,
    @Schema(title = "미디어 리스트", description = "미디어 리스트")
    List<Media> mediaList
) {
    public record Media(
        @Schema(title = "미디어 타입", description = "미디어 타입")
        MediaType mediaType,
        MultipartFile multipartFile
    ) {
    }
}
