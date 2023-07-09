package com.plee.library.dto.admin.response;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AllLoanHistoryResponse {
    private final Long id;
    private final BookInfo bookInfo;
    private final Member member;
    private final boolean isRenew;
    private final LocalDate loanedAt;
    private final LocalDate returnedAt;


    @Builder
    public AllLoanHistoryResponse(Long id, BookInfo bookInfo, Member member, boolean isRenew, LocalDate loanedAt, LocalDate returnedAt) {
        this.id = id;
        this.bookInfo = bookInfo;
        this.member = member;
        this.isRenew = isRenew;
        this.loanedAt = loanedAt;
        this.returnedAt = returnedAt;
    }
}
