package com.yaliny.autismmap.community.dto.request;

import com.yaliny.autismmap.community.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class UploadFileRequest {
    @Schema(description = "업로드할 미디어 파일 리스트", required = true)
    List<UploadFile> files;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class UploadFile {
        @Schema(title = "미디어 타입", description = "미디어 타입")
        private MediaType mediaType;

        @Schema(title = "업로드 할 실제 파일", description = "업로드 할 실제 파일")
        private MultipartFile multipartFile;

        public UploadFile(MediaType mediaType, MultipartFile multipartFile) {
            this.mediaType = mediaType;
            this.multipartFile = multipartFile;
        }
    }

    public UploadFileRequest(List<UploadFile> files) {
        this.files = files;
    }
}
