package com.plee.library.repository.member;

import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<User, Long> {
}
