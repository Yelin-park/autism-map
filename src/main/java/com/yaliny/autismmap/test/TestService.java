package com.yaliny.autismmap.test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;

    @Transactional(readOnly = true)
    void findTest() {
        testRepository.findAll();
    }
}
