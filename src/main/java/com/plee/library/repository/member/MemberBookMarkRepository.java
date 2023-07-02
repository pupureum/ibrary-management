package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberBookmark;
import com.plee.library.domain.member.MemberRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberBookMarkRepository extends JpaRepository<MemberRequestHistory, Long> {
    List<MemberBookmark> findAllByMember(Member member);

//    boolean existsByMemberAndBookInfo(Member member, BookInfo bookInfo);

//    boolean existsByMemberLoginIdAndBookInfoIsbn(Long memberId, String isbn);
}
