package com.yaliny.autismmap.community.service;

import com.yaliny.autismmap.community.dto.request.*;
import com.yaliny.autismmap.community.dto.response.PostCommentResponse;
import com.yaliny.autismmap.community.dto.response.PostDetailResponse;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.dto.response.UploadFileResponse;
import com.yaliny.autismmap.community.entity.Comment;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.entity.PostMedia;
import com.yaliny.autismmap.community.repository.CommentRepository;
import com.yaliny.autismmap.community.repository.PostRepository;
import com.yaliny.autismmap.community.service.view.ViewCountService;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.external.s3.S3Uploader;
import com.yaliny.autismmap.global.utils.SecurityUtil;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static com.yaliny.autismmap.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final S3Uploader s3Uploader;
    private final CommentRepository commentRepository;
    private final EntityManager em;
    private final ViewCountService viewCountService;

    @Transactional
    public List<UploadFileResponse> uploadFile(UploadFileRequest request) {
        return uploadPostMedias(request, "post-medias");
    }

    @Transactional
    public Long registerPost(PostCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId()).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        List<PostMedia> postMediaList = getPostMedias(request.getMediaList());
        Post post = Post.createPost(request.getTitle(), request.getContent(), member, postMediaList);
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    @Transactional(readOnly = true)
    public PostListResponse getPostList(String searchText, PageRequest pageRequest) {
        searchText = searchText != null ? searchText : "";
        Page<Post> response = postRepository.searchPost(searchText, pageRequest);
        return PostListResponse.of(response);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));
        viewCountService.bump(post.getId());
        return PostDetailResponse.ofWithOverriddenViewCount(post, post.getViewCount() + 1);
    }

    @Transactional
    public void deletePost(long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (!Objects.equals(post.getMember().getId(), memberId)) throw new CustomException(ACCESS_DENIED);
        postRepository.deleteById(postId);
    }

    @Transactional
    public PostDetailResponse updatePost(long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (!Objects.equals(post.getMember().getId(), memberId)) throw new CustomException(ACCESS_DENIED);
        List<Long> preserveIds = Optional.ofNullable(request.getPreserveMediaIds()).orElse(Collections.emptyList());
        List<PostMedia> toPreserve = post.getMediaList().stream().filter(postMedia -> preserveIds.contains(postMedia.getId())).toList();

        if (toPreserve.size() != preserveIds.size()) {
            log.warn("일부 미디어 ID가 존재하지 않습니다: 요청 preserve IDs={}, 실제 존재={}, 장소 ID={}",
                preserveIds, toPreserve.stream().map(PostMedia::getId).toList(), postId);
        }

        List<PostMedia> newMedias = getPostMedias(request.getMediaList());
        post.updatePost(request, newMedias, toPreserve);

        em.flush();

        return PostDetailResponse.of(post);
    }

    @Transactional(readOnly = true)
    public PostCommentResponse getPostComments(long postId, PageRequest pageRequest) {
        Page<Comment> parentPage = commentRepository.findAllByPostIdAndParentCommentIsNull(postId, pageRequest);
        return PostCommentResponse.of(parentPage);
    }

    @Transactional
    public long registerPostComment(long postId, PostCommentCreateRequest request) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));
        Member member = memberRepository.findById(request.memberId()).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Comment parentComment = null;

        if (request.parentCommentId() != null) {
            parentComment = commentRepository.findById(request.parentCommentId()).orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
        }

        Comment comment = Comment.createComment(request.content(), post, member, parentComment);
        commentRepository.save(comment);
        return comment.getId();
    }

    @Transactional
    public void deletePostComment(long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (!Objects.equals(comment.getMember().getId(), memberId)) throw new CustomException(ACCESS_DENIED);
        comment.deleteComment();
    }

    @Transactional
    public String updatePostComment(long commentId, PostCommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (!Objects.equals(comment.getMember().getId(), memberId)) throw new CustomException(ACCESS_DENIED);
        comment.updateComment(request.content());
        return request.content();
    }

    private List<PostMedia> getPostMedias(List<PostMediaRequest> mediaList) {
        return Optional.ofNullable(mediaList)
            .orElse(List.of())
            .stream()
            .filter(media -> !media.getUrl().isEmpty())
            .map(media -> {
                return PostMedia.createPostMedia(media.getMediaType(), media.getUrl());
            }).toList();
    }

    private List<UploadFileResponse> uploadPostMedias(UploadFileRequest request, String dirName) {
        List<UploadFileResponse> result = new ArrayList<>();

        List<UploadFileRequest.UploadFile> mediaList = Optional.ofNullable(request.getFiles())
            .orElse(List.of());

        for (UploadFileRequest.UploadFile media : mediaList) {
            MultipartFile file = media.getMultipartFile();
            if (file != null && !file.isEmpty()) {
                try {
                    String uploadedUrl = s3Uploader.upload(file, dirName);
                    result.add(new UploadFileResponse(media.getMediaType(), uploadedUrl));
                } catch (IOException e) {
                    throw new CustomException(S3_UPLOAD_FAIL);
                }
            }
        }

        return result;
    }
}
