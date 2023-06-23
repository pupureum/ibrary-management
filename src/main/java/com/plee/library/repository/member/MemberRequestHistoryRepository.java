package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRequestHistoryRepository extends JpaRepository<MemberRequestHistory, Long> {
    Optional<MemberRequestHistory> findByBookInfo(BookInfo bookInfo);
}
