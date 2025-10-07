package com.yaliny.autismmap.community.service.view;

import com.yaliny.autismmap.community.repository.PostRepository;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountService {
    private final PostRepository postRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bump(Long postId) {
        log.info("BUMP start postId={}", postId);
        int updated = postRepository.incrementViewCount(postId);
        log.info("BUMP end   postId={}, updated={}", postId, updated);
        if (updated == 0) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
    }
}
