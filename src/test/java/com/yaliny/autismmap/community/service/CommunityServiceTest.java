package com.yaliny.autismmap.community.service;

import com.yaliny.autismmap.community.dto.request.PostCreateRequest;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.entity.MediaType;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.repository.PostMediaRepository;
import com.yaliny.autismmap.community.repository.PostRepository;
import com.yaliny.autismmap.global.exception.MemberNotFoundException;
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

    @Test
    @DisplayName("게시글 등록 성공")
    void registerPost_success() {
        Member member = getMember();

        String title = "제목입니다.";
        String content = "내용입니다.";

        MockMultipartFile mockImage = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "dummy image content".getBytes()
        );

        MockMultipartFile mockVideo = new MockMultipartFile(
            "file",
            "test-video.mp4",
            "video/mp4",
            "dummy video content".getBytes()
        );

        PostCreateRequest.Media image = new PostCreateRequest.Media(
            MediaType.IMAGE,
            mockImage
        );

        PostCreateRequest.Media video = new PostCreateRequest.Media(
            MediaType.VIDEO,
            mockVideo
        );

        ArrayList<PostCreateRequest.Media> list = new ArrayList<>();
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
            .isInstanceOf(MemberNotFoundException.class)
            .hasMessage("계정이 존재하지 않습니다.");
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

    private void createDummyPost() {
        Member member = getMember();
        String title = "제목입니다.";
        String content = "내용입니다.";

        PostCreateRequest request = new PostCreateRequest(
            member.getId(),
            title,
            content,
            null
        );

        communityService.registerPost(request);
    }
}