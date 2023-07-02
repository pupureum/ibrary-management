package com.plee.library.dto.admin.response;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AllBookRequestResponse {
    private final Long id;
    private final Member member;
    private final BookInfo bookInfo;
    private final String requestReason;
    private final boolean isApproved;
    private final LocalDate requestedAt;

    @Builder
    public AllBookRequestResponse(Long id, Member member, BookInfo bookInfo, String requestReason, boolean isApproved, LocalDate requestedAt) {
        this.id = id;
        this.member = member;
        this.bookInfo = bookInfo;
        this.requestReason = requestReason;
        this.isApproved = isApproved;
        this.requestedAt = requestedAt;
    }
}
