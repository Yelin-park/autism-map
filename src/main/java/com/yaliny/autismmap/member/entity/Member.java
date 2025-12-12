package com.yaliny.autismmap.member.entity;

import com.yaliny.autismmap.community.entity.Comment;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@SQLDelete(sql = "UPDATE member SET del_yn = 1 WHERE member_id = ?")
@SQLRestriction("del_yn = 0")
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

    @Column(nullable = false)
    private int delYn = 0;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    public static Member createMember(String email, String password, String nickname) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.nickname = nickname;
        member.role = Role.USER;
        member.isSocial = false;
        return member;
    }

    public static Member createMember(String email, String password, String nickname, Role role) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.nickname = nickname;
        member.role = role;
        member.isSocial = false;
        return member;
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

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
