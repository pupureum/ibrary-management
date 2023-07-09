package com.plee.library.repository.member;

import com.plee.library.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);

    Page<Member> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByLoginId(String loginId);
}
