package com.yaliny.autismmap.community.dto.response;

import com.yaliny.autismmap.community.entity.MediaType;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.entity.PostMedia;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record PostDetailResponse(
    @Schema(title = "게시글 ID", description = "게시글 ID")
    long postId,
    @Schema(title = "게시글 제목", description = "게시글 제목")
    String title,
    @Schema(title = "게시글 조회수")
    Long viewCount,
    @Schema(title = "게시글 내용", description = "게시글 내용")
    String content,
    @Schema(title = "게시글 미디어 리스트", description = "게시글 미디어 리스트")
    List<Media> mediaList,
    @Schema(title = "등록자 ID", description = "등록자 ID")
    long memberId,
    @Schema(title = "등록자 닉네임", description = "등록자 닉네임")
    String nickName,
    @Schema(title = "등록 일시", description = "등록 일시")
    String regDateTime,
    @Schema(title = "수정 일시", description = "수정 일시")
    String modDateTime
) {
    public record Media(
        @Schema(title = "미디어 파일 ID", description = "미디어 파일 ID")
        long id,
        @Schema(title = "미디어 타입", description = "미디어 타입")
        MediaType mediaType,
        @Schema(title = "S3 URL", description = "S3 URL")
        String url
    ) {
        public static PostDetailResponse.Media of(PostMedia media) {
            return new PostDetailResponse.Media(media.getId(), media.getMediaType(), media.getUrl());
        }
    }

    public static PostDetailResponse of(Post post) {
        return new PostDetailResponse(
            post.getId(),
            post.getTitle(),
            post.getViewCount(),
            post.getContent(),
            post.getMediaList().stream().map(Media::of).toList(),
            post.getMember().getId(),
            post.getMember().getNickname(),
            post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            post.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    public static PostDetailResponse ofWithOverriddenViewCount(Post post, long overriddenViewCount) {
        return new PostDetailResponse(
            post.getId(),
            post.getTitle(),
            overriddenViewCount,
            post.getContent(),
            post.getMediaList().stream().map(Media::of).toList(),
            post.getMember().getId(),
            post.getMember().getNickname(),
            post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            post.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
}
