package com.yaliny.autismmap.community.repository;

import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.global.config.QuerydslConfig;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import(QuerydslConfig.class)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("H2 DB 연결 테스트 - Post 저장 및 조회")
    void postSaveAndFind() {
        Member member = Member.createMember(
            "test@test.com",
            "test",
            "닉네임"
        );
        memberRepository.save(member);

        String title = "제목";
        String content = "제곧내";
        Post post = Post.createPost(title, content, member);
        postRepository.save(post);

        Post findPost = postRepository.findById(post.getId()).get();
        assertThat(findPost.getTitle()).isEqualTo(title);
        assertThat(findPost.getContent()).isEqualTo(content);
    }
}