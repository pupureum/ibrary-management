package com.plee.library.dto.book.response;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.MemberLoanHistory;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static List<LoanHistoryResponse> from(List<MemberLoanHistory> histories) {
        return histories.stream()
                .map(h -> LoanHistoryResponse.builder()
                        .id(h.getId())
                        .bookInfo(h.getBookInfo())
                        .isRenew(h.isRenew())
                        .loanedAt(h.getCreatedAt().toLocalDate())
                        .returnedAt(Optional.ofNullable(h.getReturnedAt()).map(LocalDateTime::toLocalDate).orElse(null))
                        .build())
                .collect(Collectors.toList());
    }
}
