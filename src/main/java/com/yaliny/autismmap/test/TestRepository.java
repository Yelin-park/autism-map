package com.yaliny.autismmap.test;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestRepository extends JpaRepository<Test, Integer> {

    Optional<Test> findById(int id);
}
