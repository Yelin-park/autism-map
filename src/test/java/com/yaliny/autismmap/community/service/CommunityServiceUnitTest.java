package com.yaliny.autismmap.community.service;

import com.yaliny.autismmap.community.dto.request.*;
import com.yaliny.autismmap.community.dto.response.PostCommentResponse;
import com.yaliny.autismmap.community.dto.response.PostListResponse;
import com.yaliny.autismmap.community.entity.Comment;
import com.yaliny.autismmap.community.entity.MediaType;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.repository.CommentRepository;
import com.yaliny.autismmap.community.repository.PostRepository;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.global.external.service.S3Uploader;
import com.yaliny.autismmap.global.security.CustomUserDetails;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommunityService communityService;

    private Member member;
    private Post post;
    private Comment comment;

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

    @BeforeEach
    void setUp() {
        member = new Member("test@test.com", "1234", "닉네임");
        ReflectionTestUtils.setField(member, "id", 1L);

        post = mock(Post.class);
        ReflectionTestUtils.setField(post, "id", 10L);

        comment = mock(Comment.class);
        ReflectionTestUtils.setField(comment, "id", 10L);
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void registerPost_success() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(s3Uploader.upload(mockFile, "post-medias")).thenReturn("https://s3.aws.com/post.jpg");

        PostMediaRequest media = new PostMediaRequest(MediaType.IMAGE, mockFile);
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

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() {
        Post post = mock(Post.class);
        when(post.getMember()).thenReturn(member);
        setAuthentication(member);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        communityService.deletePost(10L);

        verify(postRepository).deleteById(10L);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않음")
    void deletePost_fail_not_found() {
        setAuthentication(member);
        when(postRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.deletePost(100L))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(false);

        PostMediaRequest mediaRequest = mock(PostMediaRequest.class);
        when(mediaRequest.multipartFile()).thenReturn(multipartFile);

        when(s3Uploader.upload(multipartFile, "post-medias")).thenReturn("https://s3.aws.com/post.jpg");

        PostUpdateRequest request = mock(PostUpdateRequest.class);
        when(request.preserveMediaIds()).thenReturn(List.of());
        when(request.mediaList()).thenReturn(List.of(mediaRequest));

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(post.getTitle()).thenReturn("제목");
        when(post.getContent()).thenReturn("내용");
        when(post.getMember()).thenReturn(member);
        setAuthentication(member);
        when(post.getMediaList()).thenReturn(List.of());
        when(post.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(post.getUpdatedAt()).thenReturn(LocalDateTime.now());

        communityService.updatePost(10L, request);

        verify(post).updatePost(eq(request), anyList(), eq(List.of()));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않음")
    void updatePost_fail_not_found() {
        setAuthentication(member);
        when(postRepository.findById(100L)).thenReturn(Optional.empty());

        PostUpdateRequest request = mock(PostUpdateRequest.class);

        assertThatThrownBy(() -> communityService.updatePost(100L, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getPostComment_success() {
        Comment parentComment = Comment.createComment("부모 댓글", post, member);
        ReflectionTestUtils.setField(parentComment, "id", 10L);
        ReflectionTestUtils.setField(parentComment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(parentComment, "updatedAt", LocalDateTime.now());

        PageRequest pageRequest = PageRequest.of(0, 10);
        PageImpl<Comment> commentPage = new PageImpl<>(List.of(parentComment), pageRequest, 1);

        when(commentRepository.findAllByPostIdAndParentCommentIsNull(10L, pageRequest)).thenReturn(commentPage);

        PostCommentResponse response = communityService.getPostComments(10L, pageRequest);

        assertThat(response).isNotNull();
        assertThat(response.commentList()).hasSize(1);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.last()).isTrue();
        assertThat(response.commentList().get(0).content()).isEqualTo("부모 댓글");
        verify(commentRepository).findAllByPostIdAndParentCommentIsNull(10L, pageRequest);
    }

    @Test
    @DisplayName("댓글 목록 조회 성공 - 대댓글 포함")
    void getPostCommentWithChildren_success() {
        Comment parentComment = Comment.createComment("부모 댓글", post, member);
        ReflectionTestUtils.setField(parentComment, "id", 10L);
        ReflectionTestUtils.setField(parentComment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(parentComment, "updatedAt", LocalDateTime.now());

        Comment childComment = Comment.createComment("자식 댓글", post, member, parentComment);
        ReflectionTestUtils.setField(childComment, "id", 11L);
        ReflectionTestUtils.setField(childComment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(childComment, "updatedAt", LocalDateTime.now());

        PageRequest pageRequest = PageRequest.of(0, 10);
        PageImpl<Comment> commentPage = new PageImpl<>(List.of(parentComment), pageRequest, 1);

        when(commentRepository.findAllByPostIdAndParentCommentIsNull(10L, pageRequest)).thenReturn(commentPage);

        PostCommentResponse response = communityService.getPostComments(10L, pageRequest);

        assertThat(response).isNotNull();
        assertThat(response.commentList()).hasSize(1);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.last()).isTrue();
        assertThat(response.commentList().get(0).content()).isEqualTo("부모 댓글");
        assertThat(response.commentList().get(0).childCommentList().get(0).content()).isEqualTo("자식 댓글");
        verify(commentRepository).findAllByPostIdAndParentCommentIsNull(10L, pageRequest);
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void registerPostComment_success() {
        PostCommentCreateRequest request = new PostCommentCreateRequest(10L, "댓글댓글", null);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        Comment.createComment(request.content(), post, member);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0, Comment.class);
            ReflectionTestUtils.setField(comment, "id", 100L);
            return comment;
        });

        long commentId = communityService.registerPostComment(10L, request);

        assertThat(commentId).isEqualTo(100L);
    }

    @Test
    @DisplayName("댓글 등록 실패 - 존재하지 않는 게시글")
    void registerPostComment_fail_post_not_found() {
        PostCommentCreateRequest request = new PostCommentCreateRequest(10L, "댓글댓글", null);

        assertThatThrownBy(() -> communityService.registerPostComment(101L, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 등록 실패 - 존재하지 않는 사용자")
    void registerPostComment_fail_member_not_found() {
        PostCommentCreateRequest request = new PostCommentCreateRequest(101L, "댓글댓글", null);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(memberRepository.findById(101L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.registerPostComment(10L, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 등록 실패 - 존재하지 않는 댓글")
    void registerPostComment_fail_comment_not_found() {
        PostCommentCreateRequest request = new PostCommentCreateRequest(101L, "댓글댓글", 101L);

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(memberRepository.findById(101L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> communityService.registerPostComment(100L, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deletePostComment_success() {
        when(comment.getMember()).thenReturn(member);
        setAuthentication(member);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        communityService.deletePostComment(10L);

        verify(comment).deleteComment(); // soft delete 메서드가 호출되었는지 확인
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 본인이 작성한 댓글이 아님")
    void deletePostComment_fail_access_denied() {
        when(comment.getMember()).thenReturn(member);
        Member member2 = new Member("test2@test.com", "1234", "닉네임2");
        ReflectionTestUtils.setField(member2, "id", 100L);
        setAuthentication(member2);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> communityService.deletePostComment(10L))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 댓글 존재하지 않음")
    void deletePostComment_fail_comment_not_found() {
        clearAuthentication();
        when(commentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.deletePostComment(10L))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updatePostComment_success() {
        when(comment.getMember()).thenReturn(member);
        setAuthentication(member);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        PostCommentUpdateRequest request = mock(PostCommentUpdateRequest.class);

        communityService.updatePostComment(10L, request);

        verify(comment).updateComment(request.content());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 수정 권한 없음")
    void updatePostComment_fail_access_denied() {
        clearAuthentication();
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        PostCommentUpdateRequest request = mock(PostCommentUpdateRequest.class);

        assertThatThrownBy(() -> communityService.updatePostComment(10L, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

}
