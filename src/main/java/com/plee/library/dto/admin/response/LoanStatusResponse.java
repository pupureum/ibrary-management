package com.plee.library.dto.admin.response;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class LoanStatusResponse {
    private final Long id;
    private final BookInfo bookInfo;
    private final Member member;
    private final boolean isRenew;
    private final LocalDate loanedAt;
    private final LocalDate returnedAt;


    @Builder
    public LoanStatusResponse(Long id, BookInfo bookInfo, Member member, boolean isRenew, LocalDate loanedAt, LocalDate returnedAt) {
        this.id = id;
        this.bookInfo = bookInfo;
        this.member = member;
        this.isRenew = isRenew;
        this.loanedAt = loanedAt;
        this.returnedAt = returnedAt;
    }

    public static List<LoanStatusResponse> from(Page<MemberLoanHistory> histories) {
        return histories.stream()
                .map(h -> LoanStatusResponse.builder()
                        .id(h.getId())
                        .member(h.getMember())
                        .bookInfo(h.getBookInfo())
                        .isRenew(h.isRenew())
                        .loanedAt(h.getCreatedAt().toLocalDate())
                        .returnedAt(Optional.ofNullable(h.getReturnedAt()).map(LocalDateTime::toLocalDate).orElse(null))
                        .build())
                .collect(Collectors.toList());
    }
}
