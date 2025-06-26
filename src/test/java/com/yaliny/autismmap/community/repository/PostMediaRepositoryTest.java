package com.yaliny.autismmap.community.repository;

import com.yaliny.autismmap.community.entity.MediaType;
import com.yaliny.autismmap.community.entity.Post;
import com.yaliny.autismmap.community.entity.PostMedia;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class PostMediaRepositoryTest {

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("H2 DB 연결 테스트 - PostMedia 저장 및 조회")
    void postMediaSaveAndFind() {
        Member member = new Member(
            "test@test.com",
            "test",
            "닉네임"
        );
        memberRepository.save(member);

        String title = "제목";
        String content = "제곧내";
        PostMedia media1 = PostMedia.createPostMedia(MediaType.IMAGE, "url1");
        PostMedia media2 = PostMedia.createPostMedia(MediaType.IMAGE, "url2");
        PostMedia media3 = PostMedia.createPostMedia(MediaType.VIDEO, "url3");
        ArrayList<PostMedia> postMedia = new ArrayList<>();
        postMedia.add(media1);
        postMedia.add(media2);
        postMedia.add(media3);

        Post post = Post.createPost(title, content, member, postMedia);
        postRepository.save(post);

        List<PostMedia> postMediaList = postMediaRepository.findAll();
        assertThat(postMediaList.size()).isEqualTo(3);
    }

}