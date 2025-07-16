package com.yaliny.autismmap.community.dto.request;

import com.yaliny.autismmap.community.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@Getter
@Setter
public class PostMediaRequest {
    @Schema(title = "미디어 타입", description = "미디어 타입")
    private MediaType mediaType;

    @Schema(title = "미디어 파일", description = "미디어 파일")
    private MultipartFile multipartFile;

    public PostMediaRequest(MediaType mediaType, MultipartFile multipartFile) {
        this.mediaType = mediaType;
        this.multipartFile = multipartFile;
    }
}
