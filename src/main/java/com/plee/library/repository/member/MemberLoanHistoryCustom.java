package com.plee.library.repository.member;

import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.dto.member.condition.LoanHistorySearchCondition;

import java.util.List;

public interface MemberLoanHistoryCustom {
    List<MemberLoanHistory> search(LoanHistorySearchCondition condition);
}
