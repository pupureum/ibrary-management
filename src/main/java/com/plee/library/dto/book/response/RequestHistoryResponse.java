package com.plee.library.dto.book.response;

import com.plee.library.domain.book.BookInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RequestHistoryResponse {
    private final Long id;
    private final BookInfo bookInfo;
    private final String requestReason;
    private final boolean isApproved;
    private final LocalDate requestedAt;

    @Builder
    public RequestHistoryResponse(Long id, BookInfo bookInfo, String requestReason, boolean isApproved, LocalDate requestedAt) {
        this.id = id;
        this.bookInfo = bookInfo;
        this.requestReason = requestReason;
        this.isApproved = isApproved;
        this.requestedAt = requestedAt;
    }
}
