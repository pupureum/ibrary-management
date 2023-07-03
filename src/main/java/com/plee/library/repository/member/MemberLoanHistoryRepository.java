package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberLoanHistoryRepository extends JpaRepository<MemberLoanHistory, Long> {
    List<MemberLoanHistory> findAllByMemberId(Long memberId);

    List<MemberLoanHistory> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    boolean existsByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(Long memberId, String bookInfoId);

    Optional<MemberLoanHistory> findByIdAndReturnedAtIsNull(Long historyId);

    long countByMemberIdAndReturnedAtIsNull(Long memberId);
}
