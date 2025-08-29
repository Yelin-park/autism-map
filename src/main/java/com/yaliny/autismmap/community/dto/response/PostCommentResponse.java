package com.yaliny.autismmap.community.dto.response;

import com.yaliny.autismmap.community.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record PostCommentResponse(
    @Schema(title = "현재 페이지")
    int page,
    @Schema(title = "페이지 크기")
    int size,
    @Schema(title = "총 항목 수")
    Long totalElements,
    @Schema(title = "총 페이지 수")
    int totalPages,
    @Schema(title = "마지막 페이지 여부")
    boolean last,
    @Schema(title = "댓글 리스트")
    List<ParentComment> commentList
) {
    public static PostCommentResponse of(Page<Comment> page) {
        return new PostCommentResponse(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast(),
            page.getContent().stream().filter(it -> it.getParentComment() == null).map(ParentComment::of).toList()
        );
    }

    public record ParentComment(
        @Schema(title = "댓글 ID")
        long id,
        @Schema(title = "댓글 내용")
        String content,
        @Schema(title = "댓글 작성자 ID")
        long memberId,
        @Schema(title = "댓글 작성자 닉네임")
        String nickName,
        @Schema(title = "댓글 등록일")
        String regDate,
        @Schema(title = "자식 댓글 리스트")
        List<ChildComment> childCommentList
    ) {
        public static ParentComment of(Comment comment) {
            return new ParentComment(
                comment.getId(),
                getContent(comment),
                comment.getMember().getId(),
                comment.getMember().getNickname(),
                comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                comment.getChildComments().stream().map(ChildComment::of).toList()
            );
        }
    }

    public record ChildComment(
        @Schema(title = "댓글 ID")
        long id,
        @Schema(title = "댓글 내용")
        String content,
        @Schema(title = "댓글 작성자 ID")
        long memberId,
        @Schema(title = "댓글 작성자 닉네임")
        String nickName,
        @Schema(title = "댓글 등록일")
        String regDate
    ) {
        public static ChildComment of(Comment comment) {
            return new ChildComment(
                comment.getId(),
                getContent(comment),
                comment.getMember().getId(),
                comment.getMember().getNickname(),
                comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        }
    }

    private static String getContent(Comment comment) {
        String content;
        if (comment.isDeleted()) content = "삭제된 댓글입니다.";
        else content = comment.getContent();
        return content;
    }
}
