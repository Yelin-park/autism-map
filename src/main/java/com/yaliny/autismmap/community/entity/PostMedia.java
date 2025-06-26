package com.yaliny.autismmap.community.entity;

import com.yaliny.autismmap.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter(AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PostMedia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_media_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Lob
    @Column(nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public static PostMedia createPostMedia(MediaType mediaType, String url) {
        PostMedia postMedia = new PostMedia();
        postMedia.setMediaType(mediaType);
        postMedia.setUrl(url);
        return postMedia;
    }

    // 연관 관계 편의 메서드
    public void setPost(Post post) {
        this.post = post;
        if (!post.getMediaList().contains(this)) {
            post.getMediaList().add(this);
        }
    }
}
