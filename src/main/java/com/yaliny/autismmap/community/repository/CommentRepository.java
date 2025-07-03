package com.yaliny.autismmap.community.repository;

import com.yaliny.autismmap.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {
        "member",
        "childComments",
        "childComments.member"
    })
    Page<Comment> findAllByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);
}
