package com.plee.library.repository.member;

import com.plee.library.domain.member.MemberRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRequestHistoryRepository extends JpaRepository<MemberRequestHistory, Long> {
}
