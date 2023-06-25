package com.plee.library.domain.book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "loanable_cnt", nullable = false)
    private int loanable_cnt;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Book(BookInfo bookInfo, int quantity) {
        this.bookInfo = bookInfo;
        this.quantity = quantity;
        this.loanable_cnt = quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void decreaseLoanableCnt() {
        if (this.loanable_cnt < 1) {
            throw new IllegalStateException("더 이상 대출 가능한 도서가 없습니다.");
        }
        this.loanable_cnt--;
    }

    public void increaseLoanableCnt() {
        if (this.loanable_cnt >= this.quantity) {
            throw new IllegalStateException("대여 가능한 수량이 올바르지 않습니다.");
        }
        this.loanable_cnt++;
    }

}
