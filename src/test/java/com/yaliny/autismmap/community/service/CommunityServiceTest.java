package com.yaliny.autismmap.community.service;

import com.yaliny.autismmap.community.dto.request.*;
import com.yaliny.autismmap.community.dto.response.PostCommentResponse;
import com.yaliny.autismmap.community.dto.response.PostDetailResponse;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.entity.Comment;
import com.yaliny.autismmap.community.entity.MediaType;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.entity.PostMedia;
import com.yaliny.autismmap.community.repository.CommentRepository;
import com.yaliny.autismmap.community.repository.PostMediaRepository;
import com.yaliny.autismmap.community.repository.PostRepository;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.global.security.CustomUserDetails;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.hibernate.annotations.CurrentTimestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.yaliny.autismmap.community.entity.Comment.createComment;
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

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        postMediaRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private Member getMember(String postEmail, String postNickname) {
        Member member = Member.createMember(postEmail, "test1234", postNickname);
        memberRepository.save(member);
        return member;
    }

    private Member getMember() {
        Member member = Member.createMember("test@test.com", "test1234", "닉네임");
        memberRepository.save(member);
        return member;
    }

    private Post createDummyPost() {
        Member member = getMember("test@test123.com", "닉네임123");
        String title = "제목입니다.";
        String content = "내용입니다.";

        PostCreateRequest request = new PostCreateRequest(
            member.getId(),
            title,
            content,
            null
        );

        Long postId = communityService.registerPost(request);

        return postRepository.findById(postId).get();
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

    private void setAuthentication(Member member) {
        CustomUserDetails userDetails = new CustomUserDetails(
            member.getId(),
            member.getEmail(),
            member.getRole().name(),
            List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void registerPost_success() {
        Member member = getMember();

        String title = "제목입니다.";
        String content = "내용입니다.";

        PostMediaRequest image = new PostMediaRequest(
            MediaType.IMAGE,
            "s3.image.url"
        );

        PostMediaRequest video = new PostMediaRequest(
            MediaType.VIDEO,
            "s3.video.url"
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
        assertThat(result.content().get(0).nickName()).isEqualTo("닉네임123");
        assertThat(result.content().get(0).title()).isEqualTo("제목입니다.");
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostDetail_success() {
        long dummyPostId = createDummyPost().getId();

        PostDetailResponse result = communityService.getPostDetail(dummyPostId);

        assertThat(result.title()).isEqualTo("제목입니다.");
        assertThat(result.content()).isEqualTo("내용입니다.");
        assertThat(result.nickName()).isEqualTo("닉네임123");
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
    void getPostDetail_fail_post_not_found() {
        long dummyPostId = createDummyPost().getId();

        assertThatThrownBy(() -> communityService.getPostDetail(dummyPostId + 1))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() {
        Post dummyPost = createDummyPost();
        long dummyPostId = dummyPost.getId();
        setAuthentication(dummyPost.getMember());
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
        Post dummyPost = createDummyPost();
        long dummyPostId = dummyPost.getId();
        setAuthentication(dummyPost.getMember());

        assertThatThrownBy(() -> communityService.deletePost(dummyPostId + 1))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() {
        Post dummyPost = createDummyPost();
        long dummyPostId = dummyPost.getId();
        setAuthentication(dummyPost.getMember());
        String mockImage = "imageUrl";
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
        Post dummyPost = createDummyPost();
        long dummyPostId = dummyPost.getId();
        setAuthentication(dummyPost.getMember());
        PostUpdateRequest request = new PostUpdateRequest("수정제목", "수정내용", null, List.of());

        assertThatThrownBy(() -> communityService.updatePost(dummyPostId + 1, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getPostComment_success() {
        Member member = getMember();
        String title = "제목입니다.";
        String content = "내용입니다.";

        PostCreateRequest request = new PostCreateRequest(
            member.getId(),
            title,
            content,
            null
        );

        Long postId = communityService.registerPost(request);
        Post post = postRepository.findById(postId).get();

        Comment parentComment = createComment("부모 댓글", post, member);
        Comment childComment = createComment("자식 댓글", post, member, parentComment);
        commentRepository.save(parentComment);
        commentRepository.save(childComment);

        PostCommentResponse response = communityService.getPostComments(post.getId(), PageRequest.of(0, 10));

        assertThat(response).isNotNull();
        assertThat(response.commentList()).hasSize(1);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.last()).isTrue();
        assertThat(response.commentList().get(0).content()).isEqualTo("부모 댓글");
        assertThat(response.commentList().get(0).childCommentList().get(0).content()).isEqualTo("자식 댓글");
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void registerPostComment_success() {
        Member member = getMember();
        Post post = createDummyPost();

        String content = "댓글 내용";
        Long parentCommentId = null;

        PostCommentCreateRequest request = new PostCommentCreateRequest(member.getId(), content, parentCommentId);

        long commentId = communityService.registerPostComment(post.getId(), request);

        Comment comment = commentRepository.findById(commentId).get();

        assertThat(comment).isNotNull();
        assertThat(comment.getContent()).isEqualTo(content);
        assertThat(comment.getPost().getId()).isEqualTo(post.getId());
        assertThat(comment.getMember().getId()).isEqualTo(member.getId());
        assertThat(comment.getParentComment()).isNull();
    }

    @Test
    @DisplayName("댓글 등록 실패 - 존재하지 않는 게시글")
    void registerPostComment_fail_post_not_found() {
        Member member = getMember();
        Post post = createDummyPost();

        String content = "댓글 내용";
        Long parentCommentId = null;

        PostCommentCreateRequest request = new PostCommentCreateRequest(member.getId(), content, parentCommentId);

        assertThatThrownBy(() -> communityService.registerPostComment(post.getId() + 1, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 등록 실패 - 존재하지 않는 사용자")
    void registerPostComment_fail_member_not_found() {
        Member member = getMember();
        Post post = createDummyPost();

        String content = "댓글 내용";
        Long parentCommentId = null;

        PostCommentCreateRequest request = new PostCommentCreateRequest(member.getId() + 10, content, parentCommentId);

        assertThatThrownBy(() -> communityService.registerPostComment(post.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 등록 실패 - 존재하지 않는 댓글")
    void registerPostComment_fail_comment_not_found() {
        Member member = getMember();
        Post post = createDummyPost();

        String content = "댓글 내용";
        Long parentCommentId = 1L;

        PostCommentCreateRequest request = new PostCommentCreateRequest(member.getId() + 1, content, parentCommentId);

        assertThatThrownBy(() -> communityService.registerPostComment(post.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deletePostComment_success() {
        Member member = getMember();
        setAuthentication(member);
        Post post = createDummyPost();
        Comment parentComment = createComment("부모 댓글", post, member);
        Comment childComment = createComment("자식 댓글", post, member, parentComment);
        commentRepository.save(parentComment);
        commentRepository.save(childComment);

        communityService.deletePostComment(parentComment.getId());

        Comment parentResult = commentRepository.findById(parentComment.getId()).orElse(null);
        assertThat(parentResult).isNotNull();
        assertThat(parentResult.isDeleted()).isTrue();

        Comment childResult = commentRepository.findById(childComment.getId()).orElse(null);
        assertThat(childResult).isNotNull();
        assertThat(childResult.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
    void deletePostComment_fail_comment_not_found() {
        Member member = getMember();
        setAuthentication(member);
        Post post = createDummyPost();
        Comment parentComment = createComment("부모 댓글", post, member);
        Comment childComment = createComment("자식 댓글", post, member, parentComment);
        commentRepository.save(parentComment);
        commentRepository.save(childComment);

        assertThatThrownBy(() -> communityService.deletePostComment(parentComment.getId() + 100))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    void deletePostComment_fail_access_denied() {
        Member member = getMember();
        Post post = createDummyPost();
        Member member2 = post.getMember();
        Comment parentComment = createComment("부모 댓글", post, member);
        Comment childComment = createComment("자식 댓글", post, member, parentComment);
        commentRepository.save(parentComment);
        commentRepository.save(childComment);
        setAuthentication(member2);

        assertThatThrownBy(() -> communityService.deletePostComment(parentComment.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updatePostComment_success() {
        Member member = getMember();
        setAuthentication(member);

        Post post = createDummyPost();
        Comment comment = createComment("기존 댓글", post, member);
        commentRepository.save(comment);

        PostCommentUpdateRequest request = new PostCommentUpdateRequest("수정 댓글");
        communityService.updatePostComment(comment.getId(), request);

        Comment result = commentRepository.findById(comment.getId()).orElse(null);

        assertThat(result).isNotNull();
        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getContent()).isEqualTo("수정 댓글");

        clearAuthentication();
    }

    @Test
    @DisplayName("댓글 수정 실패 - 수정 권한 없음")
    void updatePostComment_fail_access_denied() {
        Member member = getMember();
        Post post = createDummyPost();
        Member member1 = post.getMember();
        Comment comment = createComment("기존 댓글", post, member);
        commentRepository.save(comment);
        setAuthentication(member1);

        PostCommentUpdateRequest request = new PostCommentUpdateRequest("수정 댓글");

        assertThatThrownBy(() -> communityService.updatePostComment(comment.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }
}