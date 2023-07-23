package com.plee.library.dto.book.response;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.MemberRequestHistory;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<RequestHistoryResponse> from(Page<MemberRequestHistory> histories) {
        return histories.stream()
                .map(h -> RequestHistoryResponse.builder()
                        .id(h.getId())
                        .bookInfo(h.getBookInfo())
                        .requestReason(h.getRequestReason())
                        .isApproved(h.isApproved())
                        .requestedAt(h.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
    }
}
