package com.yaliny.autismmap.community.dto.response;

import com.yaliny.autismmap.community.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;

public record UploadFileResponse(
    @Schema(description = "미디어 타입", example = "IMAGE")
    MediaType mediaType,

    @Schema(description = "S3 URL", example = "https://nurean-bucket.s3.ap-northeast-2.amazonaws.com/post-medias/02098f52-ccf2-420d-a866-8c84134564e7_KakaoTalk_20250306_204914559_02.jpg")
    String url
) {
}
