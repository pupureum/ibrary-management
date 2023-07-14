package com.plee.library.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LoanStatusResponse {
    private final Map<LocalDate, Integer> dailyLoanData;
}
