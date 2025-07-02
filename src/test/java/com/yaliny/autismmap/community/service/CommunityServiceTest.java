package com.yaliny.autismmap.community.service;

import com.yaliny.autismmap.community.dto.request.PostCreateRequest;
import com.yaliny.autismmap.community.dto.request.PostMediaRequest;
import com.yaliny.autismmap.community.dto.request.PostUpdateRequest;
import com.yaliny.autismmap.community.dto.response.PostDetailResponse;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.entity.MediaType;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.entity.PostMedia;
import com.yaliny.autismmap.community.repository.PostMediaRepository;
import com.yaliny.autismmap.community.repository.PostRepository;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CommunityServiceTest {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        postMediaRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private Member getMember() {
        Member member = new Member("test@test.com", "test1234", "닉네임");
        memberRepository.save(member);
        return member;
    }

    private long createDummyPost() {
        Member member = getMember();
        String title = "제목입니다.";
        String content = "내용입니다.";

        PostCreateRequest request = new PostCreateRequest(
            member.getId(),
            title,
            content,
            null
        );

        return communityService.registerPost(request);
    }

    private static MockMultipartFile getMockMultipartFileImage() {
        MockMultipartFile mockImage = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "dummy image content".getBytes()
        );
        return mockImage;
    }

    private static MockMultipartFile getMockMultipartFileVideo() {
        MockMultipartFile mockVideo = new MockMultipartFile(
            "file",
            "test-video.mp4",
            "video/mp4",
            "dummy video content".getBytes()
        );
        return mockVideo;
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void registerPost_success() {
        Member member = getMember();

        String title = "제목입니다.";
        String content = "내용입니다.";

        MockMultipartFile mockImage = getMockMultipartFileImage();
        MockMultipartFile mockVideo = getMockMultipartFileVideo();

        PostMediaRequest image = new PostMediaRequest(
            MediaType.IMAGE,
            mockImage
        );

        PostMediaRequest video = new PostMediaRequest(
            MediaType.VIDEO,
            mockVideo
        );

        ArrayList<PostMediaRequest> list = new ArrayList<>();
        list.add(image);
        list.add(video);

        PostCreateRequest request = new PostCreateRequest(
            member.getId(),
            title,
            content,
            list
        );

        Long postId = communityService.registerPost(request);

        Post findPost = postRepository.findById(postId).get();
        assertThat(findPost).isNotNull();
        assertThat(findPost.getTitle()).isEqualTo(title);
        assertThat(findPost.getContent()).isEqualTo(content);
        assertThat(findPost.getMember().getId()).isEqualTo(member.getId());
        assertThat(findPost.getMediaList().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글 등록 실패 - 존재하지 않는 사용자")
    void registerPost_fail_member_not_found() {
        String title = "제목입니다.";
        String content = "내용입니다.";

        PostCreateRequest request = new PostCreateRequest(
            1L,
            title,
            content,
            null
        );

        assertThatThrownBy(() -> communityService.registerPost(request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getPost_success() {
        createDummyPost();

        PostListResponse result = communityService.getPostList(null, PageRequest.of(0, 10));

        assertThat(result.content().size()).isEqualTo(1);
        assertThat(result.content().get(0).nickName()).isEqualTo("닉네임");
        assertThat(result.content().get(0).title()).isEqualTo("제목입니다.");
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostDetail_success() {
        long dummyPostId = createDummyPost();

        PostDetailResponse result = communityService.getPostDetail(dummyPostId);

        assertThat(result.title()).isEqualTo("제목입니다.");
        assertThat(result.content()).isEqualTo("내용입니다.");
        assertThat(result.nickName()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
    void getPostDetail_fail_post_not_found() {
        long dummyPostId = createDummyPost();

        assertThatThrownBy(() -> communityService.getPostDetail(dummyPostId + 1))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() {
        long dummyPostId = createDummyPost();

        communityService.deletePost(dummyPostId);

        List<Post> all = postRepository.findAll();
        assertThat(all.size()).isEqualTo(0);
        assertThat(all.stream().noneMatch(post -> post.getId() == dummyPostId)).isTrue();

        List<PostMedia> mediaList = postMediaRepository.findAll();
        assertThat(mediaList.size()).isEqualTo(0);
        assertThat(mediaList.stream().noneMatch(postMedia -> postMedia.getPost().getId() == dummyPostId)).isTrue();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    void deletePost_fail_post_not_found() {
        long dummyPostId = createDummyPost();

        assertThatThrownBy(() -> communityService.deletePost(dummyPostId + 1))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() {
        long dummyPostId = createDummyPost();
        MockMultipartFile mockImage = getMockMultipartFileImage();
        PostMediaRequest postMediaRequest = new PostMediaRequest(MediaType.IMAGE, mockImage);
        PostUpdateRequest request = new PostUpdateRequest("수정제목", "수정내용", null, List.of(postMediaRequest));

        communityService.updatePost(dummyPostId, request);

        Post post = postRepository.findById(dummyPostId).get();
        assertThat(post.getTitle()).isEqualTo("수정제목");
        assertThat(post.getContent()).isEqualTo("수정내용");
        assertThat(post.getMediaList().size()).isEqualTo(1);
        assertThat(post.getMediaList().get(0).getMediaType()).isEqualTo(MediaType.IMAGE);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
    void updatePost_fail_post_not_found() {
        long dummyPostId = createDummyPost();
        PostUpdateRequest request = new PostUpdateRequest("수정제목", "수정내용", null, List.of());

        assertThatThrownBy(() -> communityService.updatePost(dummyPostId + 1, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }
}