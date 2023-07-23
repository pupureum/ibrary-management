package com.plee.library.dto.member.condition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LoanHistorySearchCondition {
    private Long memberId;
    private String bookInfoId;
    private LocalDateTime time;
    private boolean notReturned;
}
