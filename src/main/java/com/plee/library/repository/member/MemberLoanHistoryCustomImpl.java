package com.plee.library.repository.member;

import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.dto.member.condition.LoanHistorySearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.plee.library.domain.member.QMemberLoanHistory.memberLoanHistory;
import static com.plee.library.util.constant.Constants.LOAN_PERIOD;
import static com.plee.library.util.constant.Constants.RENEW_PERIOD;

@RequiredArgsConstructor
public class MemberLoanHistoryCustomImpl implements MemberLoanHistoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberLoanHistory> searchHistory(LoanHistorySearchCondition condition) {
        return buildLoanHistoryQuery(condition)
                .fetch();
    }

    @Override
    public List<MemberLoanHistory> searchOverdueHistory(LoanHistorySearchCondition condition) {
        return buildOverdueLoanHistoryQuery(condition)
                .fetch();
    }

    private JPAQuery<MemberLoanHistory> buildLoanHistoryQuery(LoanHistorySearchCondition condition) {
        return queryFactory
                .selectFrom(memberLoanHistory)
                .where(
                        memberIdEq(condition.getMemberId()),
                        bookInfoIdEq(condition.getBookInfoId()),
                        time(condition.getTime()),
                        notReturned(condition.isNotReturned())
                );
    }

    private JPAQuery<MemberLoanHistory> buildOverdueLoanHistoryQuery(LoanHistorySearchCondition condition) {
        LocalDateTime overdue = condition.getTime().minusDays(LOAN_PERIOD);
        LocalDateTime renewOverdue = condition.getTime().minusDays(LOAN_PERIOD + RENEW_PERIOD);

        return queryFactory
                .selectFrom(memberLoanHistory)
                .where(
                        notReturned(true).and(time(overdue)).and(isRenewFalse())
                        .or(notReturned(true).and(time(renewOverdue)).and(isRenewTrue()))
                );
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? memberLoanHistory.member.id.eq(memberId) : null;
    }

    private BooleanExpression bookInfoIdEq(String bookInfoId) {
        return bookInfoId != null ? memberLoanHistory.bookInfo.isbn.eq(bookInfoId) : null;
    }

    private BooleanExpression notReturned(boolean notReturned) {
        return notReturned ? memberLoanHistory.returnedAt.isNull() : null;
    }

    private BooleanExpression time(LocalDateTime time) {
        return time != null ? memberLoanHistory.createdAt.before(time) : null;
    }

    private BooleanExpression isRenewTrue() {
        return memberLoanHistory.isRenew.eq(true);
    }

    private BooleanExpression isRenewFalse() {
        return memberLoanHistory.isRenew.eq(false);
    }
}
