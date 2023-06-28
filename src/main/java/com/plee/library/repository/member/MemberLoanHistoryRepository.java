package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberLoanHistoryRepository extends JpaRepository<MemberLoanHistory, Long> {
    List<MemberLoanHistory> findAllByMember(Member member);

    boolean existsByMemberAndBookInfoAndReturnAtIsNull(Member member, BookInfo bookInfo);
}
