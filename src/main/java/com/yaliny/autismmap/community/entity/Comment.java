package com.yaliny.autismmap.community.entity;

import com.yaliny.autismmap.global.entity.BaseEntity;
import com.yaliny.autismmap.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter(AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 대댓글 구현을 위한 자기 참조 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "parentComment")
    private List<Comment> childComments = new ArrayList<>();

    public static Comment createComment(String content, Post post, Member member) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setMember(member);
        return comment;
    }

    public static Comment createComment(String content, Post post, Member member, Comment parentComment) {
        Comment comment = createComment(content, post, member);
        if (parentComment != null) comment.setParentComment(parentComment);
        return comment;
    }

    // 연관 관계 편의 메서드
    public void setMember(Member member) {
        this.member = member;
        member.getComments().add(this);
    }

    public void setPost(Post post) {
        this.post = post;
        post.getComments().add(this);
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
        if (!parentComment.getChildComments().contains(this)) {
            parentComment.getChildComments().add(this);
        }
    }

    public void deleteComment() {
        this.deleted = true;
    }

    public void updateComment(String content) {
        this.content = content;
    }
}
