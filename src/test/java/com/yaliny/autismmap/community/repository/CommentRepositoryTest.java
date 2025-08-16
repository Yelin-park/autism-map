package com.yaliny.autismmap.community.repository;

import com.yaliny.autismmap.community.entity.Comment;
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

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import(QuerydslConfig.class)
class CommentRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("H2 DB 연결 테스트 - Comment 저장 및 조회")
    void postSaveTest() {
        Member member1 = Member.createMember(
            "test1@test.com",
            "test1",
            "닉네임1"
        );

        Member member2 = Member.createMember(
            "test2@test.com",
            "test2",
            "닉네임2"
        );

        List<Member> members = Arrays.asList(member1, member2);
        memberRepository.saveAll(members);

        String title = "제목";
        String content = "제곧내";
        Post post = Post.createPost(title, content, member1);
        postRepository.save(post);

        Comment comment1 = Comment.createComment("댓글", post, member2);
        commentRepository.save(comment1);
        Comment comment2 = Comment.createComment("대댓글", post, member1, comment1);
        commentRepository.save(comment2);

        List<Comment> commentList = commentRepository.findAll();
        assertThat(commentList.size()).isEqualTo(2);

        Comment findComment2 = commentRepository.findById(comment2.getId()).get();
        assertThat(findComment2.getContent()).isEqualTo(comment2.getContent());
        assertThat(findComment2.getMember().getNickname()).isEqualTo(member1.getNickname());
        assertThat(findComment2.getParentComment().getMember().getNickname()).isEqualTo(member2.getNickname());
    }

}