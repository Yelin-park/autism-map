package com.yaliny.autismmap.member.entity;

import com.yaliny.autismmap.community.entity.Comment;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean isSocial; // true: 소셜 로그인 회원

    @Enumerated(EnumType.STRING)
    @Column
    private Provider provider;

    @Column
    private String providerId; // 구글에서 제공하는 sub 값 (고유 ID)

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    public Member(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.USER;
        this.isSocial = false;
    }

    public Member(String email, String password, String nickname, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.isSocial = false;
    }

    public static Member socialSignup(String email, String nickname, Provider provider, String providerId) {
        Member member = new Member();
        member.email = email;
        member.password = null;
        member.nickname = nickname;
        member.role = Role.USER;
        member.isSocial = true;
        member.provider = provider;
        member.providerId = providerId;
        return member;
    }
}
