package com.plee.library.repository.member;

import com.plee.library.domain.member.MemberLoanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberLoanHistoryRepository extends JpaRepository<MemberLoanHistory, Long> {
}
