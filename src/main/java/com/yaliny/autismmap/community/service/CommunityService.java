package com.yaliny.autismmap.community.service;

import com.yaliny.autismmap.community.repository.CommentRepository;
import com.yaliny.autismmap.community.repository.PostMediaRepository;
import com.yaliny.autismmap.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final CommentRepository commentRepository;


}
