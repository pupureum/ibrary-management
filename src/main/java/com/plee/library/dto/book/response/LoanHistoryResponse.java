package com.plee.library.dto.book.response;

import com.plee.library.domain.book.BookInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class LoanHistoryResponse {
    private final Long id;
    private final BookInfo bookInfo;
    private final boolean isRenew;
    private final LocalDate loanedAt;
    private final LocalDate returnedAt;

    @Builder
    public LoanHistoryResponse(Long id, BookInfo bookInfo, boolean isRenew, LocalDate loanedAt, LocalDate returnedAt) {
        this.id = id;
        this.bookInfo = bookInfo;
        this.isRenew = isRenew;
        this.loanedAt = loanedAt;
        this.returnedAt = returnedAt;
    }
}
