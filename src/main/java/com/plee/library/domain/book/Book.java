package com.plee.library.domain.book;

import com.plee.library.domain.BaseTimeEntity;
import com.plee.library.message.BookMsg;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "book")
public class Book extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_seq", updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_info_isbn", nullable = false)
    private BookInfo bookInfo;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "loanable_cnt", nullable = false)
    private int loanableCnt;

    @Version
    private Long version;

    @Builder
    public Book(Long id, BookInfo bookInfo, int quantity) {
        this.id = id;
        this.bookInfo = bookInfo;
        this.quantity = quantity;
        this.loanableCnt = quantity;
        this.createdAt = LocalDateTime.now();
    }

    public void setQuantity(int quantity) {
        int diff = quantity - this.quantity;
        this.quantity = quantity;
        if (diff < 0) {
            this.loanableCnt -= Math.abs(diff);
            return;
        }
        this.loanableCnt += diff;
    }

    public void decreaseLoanableCnt() {
        if (this.loanableCnt < 1) {
            throw new IllegalStateException(BookMsg.CANNOT_LOAN_BOOK.getMessage());
        }
        this.loanableCnt -= 1;
    }

    public void increaseLoanableCnt() {
        if (this.loanableCnt >= this.quantity) {
            throw new IllegalStateException(BookMsg.INVALID_LOANABLE_CNT.getMessage());
        }
        this.loanableCnt += 1;
    }

}
