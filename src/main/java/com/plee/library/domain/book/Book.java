package com.plee.library.domain.book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

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

    @Column(name = "stock_amt", nullable = false)
    private int stock_amt;

    @Column(name = "loanable_cnt", nullable = false)
    private int loanable_cnt;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public Book(BookInfo bookInfo, int stock_amt) {
        this.bookInfo = bookInfo;
        this.stock_amt = stock_amt;
        this.loanable_cnt = stock_amt;
    }

    public void setStock_amt(int stock_amt) {
        this.stock_amt = stock_amt;
    }

    public void decreaseLoanableCnt() {
        if (this.loanable_cnt < 1) {
            throw new IllegalArgumentException("대출 가능한 도서가 없습니다.");
        }
        this.loanable_cnt--;
    }

}
