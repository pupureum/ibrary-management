package com.plee.library.dto.admin.response;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberRequestHistory;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RequestStatusResponse {
    private final Long id;
    private final Member member;
    private final BookInfo bookInfo;
    private final String requestReason;
    private final boolean isApproved;
    private final LocalDate requestedAt;

    @Builder
    public RequestStatusResponse(Long id, Member member, BookInfo bookInfo, String requestReason, boolean isApproved, LocalDate requestedAt) {
        this.id = id;
        this.member = member;
        this.bookInfo = bookInfo;
        this.requestReason = requestReason;
        this.isApproved = isApproved;
        this.requestedAt = requestedAt;
    }

    public static List<RequestStatusResponse> from(Page<MemberRequestHistory> histories) {
        return histories.stream()
                .map(h -> RequestStatusResponse.builder()
                        .id(h.getId())
                        .member(h.getMember())
                        .bookInfo(h.getBookInfo())
                        .requestReason(h.getRequestReason())
                        .isApproved(h.isApproved())
                        .requestedAt(h.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
    }
}
