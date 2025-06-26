package com.yaliny.autismmap.community.repository;

import com.yaliny.autismmap.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
