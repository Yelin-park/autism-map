package com.yaliny.autismmap.community.service;

import com.yaliny.autismmap.community.dto.request.PostCreateRequest;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.entity.MediaType;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.repository.PostRepository;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.global.external.service.S3Uploader;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommunityServiceUnitTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private CommunityService communityService;

    private Member member;
    private Post post;
    private PostListResponse postListResponse;

    @BeforeEach
    void setUp() {
        member = new Member("test@test.com", "1234", "닉네임");
        ReflectionTestUtils.setField(member, "id", 1L);

        post = mock(Post.class);
        ReflectionTestUtils.setField(post, "id", 10L);
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void registerPost_success() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(s3Uploader.upload(mockFile, "post-medias")).thenReturn("https://s3.aws.com/post.jpg");

        PostCreateRequest.Media media = new PostCreateRequest.Media(MediaType.IMAGE, mockFile);
        PostCreateRequest request = new PostCreateRequest(1L, "제목", "내용", List.of(media));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0, Post.class);
            ReflectionTestUtils.setField(post, "id", 100L);
            return post;
        });

        Long savedPostId = communityService.registerPost(request);

        assertThat(savedPostId).isEqualTo(100L);
        verify(postRepository).save(any(Post.class));
        verify(s3Uploader).upload(mockFile, "post-medias");
    }

    @Test
    @DisplayName("게시글 등록 실패 - 회원이 존재하지 않음")
    void registerPost_fail_no_member() {
        PostCreateRequest request = new PostCreateRequest(999L, "제목", "내용", null);
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.registerPost(request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getPostList_success() {
        when(post.getTitle()).thenReturn("제목");
        when(post.getContent()).thenReturn("내용");
        when(post.getMember()).thenReturn(member);
        when(post.getMediaList()).thenReturn(List.of());
        when(post.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(post.getUpdatedAt()).thenReturn(LocalDateTime.now());

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Post> page = new PageImpl<>(List.of(post), pageRequest, 1);

        when(postRepository.searchPost("", pageRequest)).thenReturn(page);

        PostListResponse response = communityService.getPostList(null, pageRequest);

        assertThat(response.page()).isEqualTo(0);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.last()).isTrue();
        assertThat(response.content().get(0).title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostDetail_success() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(post.getTitle()).thenReturn("제목");
        when(post.getContent()).thenReturn("내용");
        when(post.getMember()).thenReturn(member);
        when(post.getMediaList()).thenReturn(List.of());
        when(post.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(post.getUpdatedAt()).thenReturn(LocalDateTime.now());

        communityService.getPostDetail(10L);

        verify(postRepository).findById(10L);
    }
    
    @Test
    @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
    void getPostDetail_fail_not_found() {
        when(postRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.getPostDetail(100L))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

}
