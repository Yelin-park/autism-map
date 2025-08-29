package com.yaliny.autismmap.community.controller;

import com.yaliny.autismmap.community.dto.request.*;
import com.yaliny.autismmap.community.dto.response.PostCommentResponse;
import com.yaliny.autismmap.community.dto.response.PostDetailResponse;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.dto.response.UploadFileResponse;
import com.yaliny.autismmap.community.service.CommunityService;
import com.yaliny.autismmap.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "커뮤니티 관리 기능")
@RequestMapping("/api/v1/community")
@RestController
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "미디어 파일 등록", description = "이미지, 동영상 업로드 API")
    @PostMapping(value = "/posts/file")
    public ResponseEntity<BaseResponse<List<UploadFileResponse>>> uploadFile(@ModelAttribute UploadFileRequest request) {
        List<UploadFileResponse> response = communityService.uploadFile(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "게시글 등록")
    @PostMapping(value = "/posts")
    public ResponseEntity<BaseResponse<String>> registerPost(
        @RequestBody PostCreateRequest request
    ) {
        long postId = communityService.registerPost(request);
        return ResponseEntity.ok(BaseResponse.success("postId: " + postId + " 게시글 등록 성공"));
    }

    @Operation(summary = "게시글 목록 조회")
    @GetMapping(value = "/posts")
    public ResponseEntity<BaseResponse<PostListResponse>> getPostList(
        @Parameter(description = "검색어가 없으면 null로 주세요.")
        @RequestParam(required = false) String searchText,
        @Parameter(description = "페이지")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "요청 개수")
        @RequestParam(defaultValue = "10") int size
    ) {
        PostListResponse response = communityService.getPostList(searchText, PageRequest.of(page, size));
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping(value = "/posts/{postId}")
    public ResponseEntity<BaseResponse<PostDetailResponse>> getPostDetail(
        @PathVariable Long postId
    ) {
        PostDetailResponse response = communityService.getPostDetail(postId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping(value = "/posts/{postId}")
    public ResponseEntity<BaseResponse<String>> deletePost(
        @PathVariable Long postId
    ) {
        communityService.deletePost(postId);
        return ResponseEntity.ok(BaseResponse.success("postId: " + postId + " 게시글 삭제 성공"));
    }

    @Operation(summary = "게시글 수정")
    @PatchMapping(value = "/posts/{postId}")
    public ResponseEntity<BaseResponse<PostDetailResponse>> updatePost(
        @PathVariable Long postId,
        @RequestBody PostUpdateRequest request
    ) {
        PostDetailResponse response = communityService.updatePost(postId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping(value = "/posts/{postId}/comments")
    public ResponseEntity<BaseResponse<PostCommentResponse>> getPostComments(
        @PathVariable Long postId,
        @Parameter(description = "페이지")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "요청 개수")
        @RequestParam(defaultValue = "10") int size
    ) {
        PostCommentResponse response = communityService.getPostComments(postId, PageRequest.of(page, size));
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "댓글 등록")
    @PostMapping(value = "/posts/{postId}/comment")
    public ResponseEntity<BaseResponse<String>> registerPostComment(
        @PathVariable Long postId,
        @RequestBody PostCommentCreateRequest request
    ) {
        long commentId = communityService.registerPostComment(postId, request);
        return ResponseEntity.ok(BaseResponse.success("commentId: " + commentId + " 댓글 등록 성공"));
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping(value = "/comments/{commentId}")
    public ResponseEntity<BaseResponse<String>> registerPostComment(
        @PathVariable Long commentId
    ) {
        communityService.deletePostComment(commentId);
        return ResponseEntity.ok(BaseResponse.success("commentId: " + commentId + " 댓글 삭제 성공"));
    }

    @Operation(summary = "댓글 수정")
    @PatchMapping(value = "/comments/{commentId}")
    public ResponseEntity<BaseResponse<String>> updatePostComment(
        @PathVariable Long commentId,
        @RequestBody PostCommentUpdateRequest request
    ) {
        String response = communityService.updatePostComment(commentId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
