package com.plee.library.domain.book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_seq", updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_info_isbn", nullable = false)
    private BookInfo bookInfo;

    @Column(name = "stock", nullable = false)
    private int stock;

    @ColumnDefault("true")
    @Column(name = "is_loanable", nullable = false)
    private boolean isLoanable;

    @Builder
    public Book(BookInfo bookInfo, int stock, boolean isLoanable) {
        this.bookInfo = bookInfo;
        this.stock = stock;
        this.isLoanable = isLoanable;
    }
}
