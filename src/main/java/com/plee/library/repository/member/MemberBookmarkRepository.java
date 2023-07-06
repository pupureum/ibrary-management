package com.plee.library.repository.member;

import com.plee.library.domain.member.MemberBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBookmarkRepository extends JpaRepository<MemberBookmark, Long> {
    Page<MemberBookmark> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndBookId(Long memberId, Long bookId);

    void deleteByMemberIdAndBookId(Long memberId, Long bookId);

//    boolean existsByMemberLoginIdAndBookInfoIsbn(Long memberId, String isbn);
}
