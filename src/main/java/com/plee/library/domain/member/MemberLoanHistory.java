package com.plee.library.domain.member;

import com.plee.library.domain.BaseTimeEntity;
import com.plee.library.domain.book.BookInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DynamicInsert
@Table(name = "member_loan_history")
public class MemberLoanHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_loan_his_seq", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_info_isbn")
    private BookInfo bookInfo;

    @Column(name = "is_renew")
    @ColumnDefault("false")
    private boolean isRenew;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Builder
    public MemberLoanHistory(Member member, BookInfo bookInfo) {
        this.member = member;
        this.bookInfo = bookInfo;
    }

    public void doRenew() {
        this.isRenew = true;
    }

    public void doReturn() {
        this.returnedAt = LocalDateTime.now();
    }

    public boolean isRenewable() {
        return !isRenew;
    }

//    public boolean isOverDue() {
//        LocalDate dueDate = loanedAt.plusDays(isRenew ? RENEWAL_LIMIT : LOANABLE_DAYS);
//        LocalDate now = LocalDate.now();
//        return now.isAfter(dueDate);
//    }

    public boolean isReturned() {
        return returnedAt != null;
    }
}
