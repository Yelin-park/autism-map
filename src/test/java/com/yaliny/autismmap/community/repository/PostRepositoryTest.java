package com.yaliny.autismmap.community.repository;

import com.yaliny.autismmap.community.entity.CategoryType;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.global.config.QuerydslConfig;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        CategoryType category = CategoryType.FREE;
        Post post = Post.createPost(category, title, content, member);
        postRepository.save(post);

        Post findPost = postRepository.findById(post.getId()).get();
        assertThat(findPost.getTitle()).isEqualTo(title);
        assertThat(findPost.getContent()).isEqualTo(content);
        assertThat(findPost.getCategory()).isEqualTo(category);
    }

    @Test
    @DisplayName("카테고리 필터는 제목 검색 결과 전체에 적용된다")
    void searchPost_appliesCategoryFilterToWholeSearchCondition() {
        Member member = Member.createMember("filter@test.com", "test", "writer");
        memberRepository.save(member);

        Post freePost = Post.createPost(CategoryType.FREE, "같은검색어", "free content", member);
        Post questionPost = Post.createPost(CategoryType.QNA, "같은검색어", "question content", member);

        postRepository.save(freePost);
        postRepository.save(questionPost);

        Page<Post> result = postRepository.searchPost(
            CategoryType.FREE,
            "같은검색어",
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).extracting(Post::getCategory)
            .containsExactly(CategoryType.FREE);
    }
}
