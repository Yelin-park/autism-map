package com.yaliny.autismmap.member.repository;

import com.yaliny.autismmap.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
