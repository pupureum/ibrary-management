package com.plee.library.domain.book;

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
    private int loanableCnt;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public Book(BookInfo bookInfo, int quantity) {
        this.bookInfo = bookInfo;
        this.quantity = quantity;
        this.loanableCnt = quantity;
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
            throw new IllegalStateException("더 이상 대출 가능한 도서가 없습니다.");
        }
        this.loanableCnt--;
    }

    public void increaseLoanableCnt() {
        if (this.loanableCnt >= this.quantity) {
            throw new IllegalStateException("대여 가능한 수량이 올바르지 않습니다.");
        }
        this.loanableCnt++;
    }

}
