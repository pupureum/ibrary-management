package com.plee.library.domain.member;

import com.plee.library.domain.BaseTimeEntity;
import com.plee.library.domain.book.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member_bookmark",
        uniqueConstraints = {
                @UniqueConstraint(name = "member_book_info_unique", columnNames = {"member_seq", "book_seq"})
        })
public class MemberBookmark extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_bookmark_seq", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_seq")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @Builder
    public MemberBookmark(Member member, Book book) {
        this.member = member;
        this.book = book;
    }
}
