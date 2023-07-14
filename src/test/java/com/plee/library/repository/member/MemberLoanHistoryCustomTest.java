package com.plee.library.repository.member;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.dto.member.condition.LoanHistorySearchCondition;
import com.plee.library.repository.book.BookInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestJPAConfig.class)
@DisplayName("MemberLoanHistoryCustom 테스트")
class MemberLoanHistoryCustomTest {

    @Autowired
    MemberLoanHistoryRepository memberLoanHisRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BookInfoRepository bookInfoRepository;

    private Member member;
    private MemberLoanHistory loanHistory1;
    private MemberLoanHistory loanHistory2;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .name("member1")
                .loginId("member1@gmail.com")
                .password("123456")
                .build();
        memberRepository.save(member);

        BookInfo bookInfo = BookInfo.builder()
                .isbn("9788994492081")
                .title("bookInfo")
                .author("test")
                .build();
        bookInfoRepository.save(bookInfo);

        loanHistory1 = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .build();
        memberLoanHisRepository.save(loanHistory1);

        BookInfo bookInfo2 = BookInfo.builder()
                .isbn("9988994492082")
                .title("bookInfo2")
                .author("test2")
                .build();
        bookInfoRepository.save(bookInfo2);

        loanHistory2 = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo2)
                .build();
        memberLoanHisRepository.save(loanHistory2);
    }

    @Nested
    @DisplayName("특정 회원의 대출 내역 조회")
    class MemberLoanHistoryTest {

        @Test
        @DisplayName("모든 대출 내역 조회")
        void searchAllMemberLoanHistory() {
            // when
            List<MemberLoanHistory> result = memberLoanHisRepository.searchHistory(LoanHistorySearchCondition.builder()
                    .memberId(member.getId())
                    .build());

            // then
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(2);
            assertThat(result).contains(loanHistory1, loanHistory2);
        }

        @Test
        @DisplayName("대출중인 도서만 조회")
        void searchOnLoanHistory() {
            // given
            // loanHistory1은 반납 처리
            loanHistory1.doReturn();
            memberLoanHisRepository.save(loanHistory1);

            // when
            List<MemberLoanHistory> result = memberLoanHisRepository.searchHistory(LoanHistorySearchCondition.builder()
                    .memberId(member.getId())
                    .notReturned(true)
                    .build());

            // then
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);
            assertThat(result).containsExactly(loanHistory2);
        }
    }

    @Nested
    @DisplayName("연체 도서 조회")
    class SearchOverdueHistoryTest {

        @Test
        @DisplayName("모두 대출 기간이 지난 경우")
        void searchOverdueHistory() {
            // when
            // 7일 뒤 기준 연체된 도서 조회
            List<MemberLoanHistory> overdueHistories = memberLoanHisRepository.searchOverdueHistory(LoanHistorySearchCondition.builder()
                    .time(LocalDateTime.now().plusDays(7))
                    .build());

            // then
            assertThat(overdueHistories).isNotNull();
            assertThat(overdueHistories.size()).isEqualTo(2);
            assertThat(overdueHistories).contains(loanHistory1, loanHistory2);
        }

        @Test
        @DisplayName("대출 연장으로 기간이 늘어난 경우")
        void searchOverdueHistory_containRenew() {
            // given
            // loanHistory1 기간 연장
            loanHistory1.doRenew();

            // when
            List<MemberLoanHistory> overdueHistories = memberLoanHisRepository.searchOverdueHistory(LoanHistorySearchCondition.builder()
                    .time(LocalDateTime.now().plusDays(7))
                    .build());

            // then
            assertThat(overdueHistories).isNotNull();
            assertThat(overdueHistories.size()).isEqualTo(1);
            assertThat(overdueHistories).containsExactly(loanHistory2);
        }

        @Test
        @DisplayName("연체된 도서가 없는 경우")
        void searchOverdueHistory_notExist() {
            // when
            List<MemberLoanHistory> overdueHistories = memberLoanHisRepository.searchOverdueHistory(LoanHistorySearchCondition.builder()
                    .time(LocalDateTime.now())
                    .build());

            // then
            assertThat(overdueHistories).isEmpty();
        }
    }
}