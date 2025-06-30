package com.yaliny.autismmap.community.repository;

import com.yaliny.autismmap.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> searchPost(String searchText, Pageable pageable);
}
