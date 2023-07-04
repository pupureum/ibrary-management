package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberLoanHistoryRepository extends JpaRepository<MemberLoanHistory, Long> {
    List<MemberLoanHistory> findAllByMemberId(Long memberId);

    Page<MemberLoanHistory> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(Long memberId, String bookInfoId);

    Optional<MemberLoanHistory> findByIdAndReturnedAtIsNull(Long historyId);

    long countByMemberIdAndReturnedAtIsNull(Long memberId);
}
