package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberLoanHistoryRepository extends JpaRepository<MemberLoanHistory, Long>, QuerydslPredicateExecutor<MemberLoanHistoryRepository>, MemberLoanHistoryCustom {
    boolean existsByBookInfoIsbnAndReturnedAtIsNull(String bookInfoId);

    long countByMemberIdAndReturnedAtIsNull(Long memberId);

    @Query("SELECT Date(h.createdAt), COUNT(h) FROM MemberLoanHistory h WHERE Date(h.createdAt) >= :startDate AND Date(h.createdAt) <= :endDate GROUP BY Date(h.createdAt)")
    List<Object[]> countGroupByCreatedAtRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Page<MemberLoanHistory> findAllByMemberId(Long memberId, Pageable pageable);
    Optional<MemberLoanHistory> findByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(Long memberId, String bookInfoId);
}
