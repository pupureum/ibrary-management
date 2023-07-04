package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberRequestHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRequestHistoryRepository extends JpaRepository<MemberRequestHistory, Long> {
    boolean existsByBookInfoIsbnAndIsApprovedFalse(String isbn);

    Page<MemberRequestHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<MemberRequestHistory> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndBookInfoIsbn(Long memberId, String isbn);

    @Modifying
    @Query("UPDATE MemberRequestHistory m SET m.isApproved = true WHERE m.bookInfo.isbn = :isbn")
    void approveByBookInfoIsbn(String isbn);
}
