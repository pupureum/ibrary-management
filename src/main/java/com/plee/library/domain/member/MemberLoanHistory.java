package com.plee.library.domain.member;

import com.plee.library.domain.book.Book;
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
@Entity
@DynamicInsert
@Table(name = "member_loan_history")
public class MemberLoanHistory {

    @PrePersist
    public void setReturnDate() {
        this.returnDate = this.loanDate.plusDays(15);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_loan_his_seq", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_seq")
    private Book book;

    @Column(name = "is_return")
    @ColumnDefault("false")
    private boolean isReturn;

    @Column(name = "is_return")
    @ColumnDefault("false")
    private boolean isRenew;

    @CreatedDate
    @Column(name = "loan_date")
    private LocalDateTime loanDate;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Builder
    public MemberLoanHistory(Member member, Book book) {
        this.member = member;
        this.book = book;
    }

    public void doReturn() {
        this.isReturn = true;
    }

    public void doRenew() {
        this.isRenew = true;
        this.returnDate = this.returnDate.plusDays(7);
    }
}
