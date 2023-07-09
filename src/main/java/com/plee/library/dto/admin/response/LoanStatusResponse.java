package com.plee.library.dto.admin.response;

import com.plee.library.domain.member.MemberLoanHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LoanStatusResponse {
    private final Map<LocalDate, Integer> dailyLoanData;
}
