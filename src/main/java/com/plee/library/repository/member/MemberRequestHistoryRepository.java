package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberRequestHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MemberRequestHistoryRepository extends JpaRepository<MemberRequestHistory, Long> {

    boolean existsByBookInfoIsbnAndIsApprovedFalse(String isbn);

    boolean existsByMemberIdAndBookInfoIsbn(Long memberId, String isbn);

    boolean existsByBookInfoIsbn(String isbn);

    Page<MemberRequestHistory> findAllByMemberId(Long memberId, Pageable pageable);

    List<MemberRequestHistory> findByMemberIdAndIsApprovedFalse(Long memberId);

    long countByBookInfo(BookInfo bookInfo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MemberRequestHistory m SET m.isApproved = true WHERE m.bookInfo.isbn = :isbn AND m.isApproved = false")
    void approveByBookInfoIsbn(String isbn);
}
