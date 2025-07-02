package com.yaliny.autismmap.community.dto.request;

import com.yaliny.autismmap.community.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record PostMediaRequest(
    @Schema(title = "미디어 타입", description = "미디어 타입")
    MediaType mediaType,
    @Schema(title = "미디어 파일", description = "미디어 파일")
    MultipartFile multipartFile
) {
}
