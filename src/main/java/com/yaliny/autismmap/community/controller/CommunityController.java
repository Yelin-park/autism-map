package com.yaliny.autismmap.community.controller;

import com.yaliny.autismmap.community.dto.request.PostCreateRequest;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.service.CommunityService;
import com.yaliny.autismmap.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "커뮤니티 관리 기능")
@RequestMapping("/api/v1/community")
@RestController
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "게시글 등록")
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<String>> registerPost(
        @ModelAttribute PostCreateRequest request) {
        Long postId = communityService.registerPost(request);
        return ResponseEntity.ok(BaseResponse.success("postId: " + postId + " 게시글 등록 성공"));
    }

    @Operation(summary = "게시글 목록 조회")
    @GetMapping(value = "/posts")
    public ResponseEntity<BaseResponse<PostListResponse>> getPostList() {
        PostListResponse response = communityService.getPostList();
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
