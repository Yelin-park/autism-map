package com.yaliny.autismmap.community.service;

import com.yaliny.autismmap.community.dto.request.PostCreateRequest;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.entity.PostMedia;
import com.yaliny.autismmap.community.repository.PostRepository;
import com.yaliny.autismmap.global.exception.MemberNotFoundException;
import com.yaliny.autismmap.global.exception.S3FileUploadFailedException;
import com.yaliny.autismmap.global.external.service.S3Uploader;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public Long registerPost(PostCreateRequest request) {
        Member member = memberRepository.findById(request.memberId()).orElseThrow(MemberNotFoundException::new);
        List<PostMedia> postMediaList = uploadPostMedias(request.mediaList(), "post-medias");
        Post post = Post.createPost(request.title(), request.content(), member, postMediaList);
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    private List<PostMedia> uploadPostMedias(List<PostCreateRequest.Media> mediaList, String dirName) {
        return Optional.ofNullable(mediaList)
            .orElse(List.of())
            .stream()
            .filter(media -> !media.multipartFile().isEmpty())
            .map(media -> {
                String uploadedUrl;
                try {
                    uploadedUrl = s3Uploader.upload(media.multipartFile(), dirName);
                } catch (IOException e) {
                    throw new S3FileUploadFailedException();
                }
                return PostMedia.createPostMedia(media.mediaType(), uploadedUrl);
            }).toList();
    }

    @Transactional(readOnly = true)
    public PostListResponse getPostList() {
        return null;
    }
}
