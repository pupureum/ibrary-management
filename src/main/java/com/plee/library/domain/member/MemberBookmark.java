package com.plee.library.domain.member;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member_bookmark",
        uniqueConstraints = {
                @UniqueConstraint(name = "member_book_info_unique", columnNames = {"member_seq", "book_info_isbn"})
        })
public class MemberBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_bookmark_seq", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_info_isbn")
    private BookInfo bookInfo;

    @CreatedDate
    @Column(name = "bookmarked_at")
    private LocalDateTime bookmarkedAt;

    @Builder
    public MemberBookmark(Member member, BookInfo bookInfo) {
        this.member = member;
        this.bookInfo = bookInfo;
    }
}
