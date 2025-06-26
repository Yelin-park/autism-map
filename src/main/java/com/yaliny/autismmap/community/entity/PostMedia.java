package com.yaliny.autismmap.community.entity;

import com.yaliny.autismmap.global.entity.BaseEntity;
import jakarta.persistence.*;

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
}
