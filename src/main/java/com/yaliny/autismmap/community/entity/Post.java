package com.yaliny.autismmap.community.entity;

import com.yaliny.autismmap.community.dto.request.PostUpdateRequest;
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
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostMedia> mediaList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public static Post createPost(String title, String content, Member member) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setMember(member);
        return post;
    }

    public static Post createPost(String title, String content, Member member, List<PostMedia> medias) {
        Post post = createPost(title, content, member);
        for (PostMedia media : medias) {
            post.addMedia(media);
        }
        return post;
    }

    // 연관 관계 편의 메서드
    public void setMember(Member member) {
        this.member = member;
        member.getPosts().add(this);
    }

    public void addMedia(PostMedia media) {
        this.mediaList.add(media);
        media.setPost(this);
    }

    public void updatePost(PostUpdateRequest request, List<PostMedia> newMedias, List<PostMedia> toPreserve) {
        this.title = request.title();
        this.content = request.content();
        this.mediaList.clear();
        for (PostMedia media : toPreserve) {
            this.addMedia(media);
        }

        for (PostMedia media : newMedias) {
            this.addMedia(media);
        }
    }
}
