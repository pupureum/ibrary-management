package com.plee.library.repository.member;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.repository.book.BookInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestJPAConfig.class)
@DisplayName("MemberLoanHistoryRepository 테스트")
class MemberLoanHistoryRepositoryTest {
    @Autowired
    private MemberLoanHistoryRepository memberLoanHisRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BookInfoRepository bookInfoRepository;

    private Member member;
    private BookInfo bookInfo;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .name("이푸름")
                .loginId("test@gmail.com")
                .password("test12")
                .build();
        memberRepository.save(member);

        bookInfo = BookInfo.builder()
                .isbn("9788994492081")
                .title("bookInfo")
                .author("info")
                .publisher("info")
                .description("책 소개입니다")
                .pubDate("20221211")
                .build();
        bookInfoRepository.save(bookInfo);
    }

    @Test
    @DisplayName("회원 대출 내역 생성 테스트")
    void saveMemberLoanHistory() {
        // given
        MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .build();

        // when
        MemberLoanHistory savedLoanHistory = memberLoanHisRepository.save(loanHistory);

        // then
        assertThat(savedLoanHistory).isNotNull();
        assertThat(savedLoanHistory).isEqualTo(loanHistory);
        assertThat(savedLoanHistory.getId()).isEqualTo(loanHistory.getId());
        assertThat(savedLoanHistory.isRenew()).isEqualTo(loanHistory.isRenew());
        assertThat(savedLoanHistory.getReturnedAt()).isEqualTo(loanHistory.getReturnedAt());
    }

    @Nested
    @DisplayName("특정 기록 존재 여부 확인 테스트")
    public class isExistNotReturnedTest {
        @Test
        @DisplayName("회원과 도서 정보로 대출중인 기록 확인")
        void existsByMemberIdAndBookInfoIsbnAndReturnedAtIsNull() {
            // given
            MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .build();
            loanHistory.doReturn();
            memberLoanHisRepository.save(loanHistory);

            // when
            boolean result = memberLoanHisRepository.existsByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(member.getId(), bookInfo.getIsbn());

            // then
            assertThat(result).isFalse();

        }

        @Test
        @DisplayName("도서 정보로 대출중인 도서 존재 여부 확인")
        void existsByBookInfoIsbnAndReturnedAtIsNull() {
            // given
            MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .build();
            loanHistory.doReturn();
            memberLoanHisRepository.save(loanHistory);

            // when
            boolean result = memberLoanHisRepository.existsByBookInfoIsbnAndReturnedAtIsNull(bookInfo.getIsbn());

            // then
            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("회원의 모든 대출 내역 최신순 조회")
    void findAllByMemberId() {
        // given
        MemberLoanHistory loanHistory1 = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .build();
        memberLoanHisRepository.save(loanHistory1);

        BookInfo bookInfo2 = BookInfo.builder()
                .isbn("9788994492083")
                .title("bookInfo2")
                .author("info")
                .publisher("info")
                .description("책 소개입니다")
                .build();
        bookInfoRepository.save(bookInfo2);
        MemberLoanHistory loanHistory2 = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo2)
                .build();
        memberLoanHisRepository.save(loanHistory2);

        // when
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<MemberLoanHistory> result = memberLoanHisRepository.findAllByMemberId(member.getId(), pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).contains(loanHistory1, loanHistory2);

        assertThat(result.getContent().get(0)).isEqualTo(loanHistory2);
        assertThat(result.getContent().get(1)).isEqualTo(loanHistory1);
    }

    @Test
    @DisplayName("특정 회원의 미반납 대출 기록 개수 조회")
    void countByMemberIdAndReturnedAtIsNull() {
        // given
        MemberLoanHistory loanHistory1 = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .build();
        memberLoanHisRepository.save(loanHistory1);

        // when
        long result = memberLoanHisRepository.countByMemberIdAndReturnedAtIsNull(member.getId());

        // then
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("대출 연장 상태 업데이트")
    void doRenew() {
        // given
        MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .build();
        memberLoanHisRepository.save(loanHistory);

        // when
        loanHistory.doRenew();
        MemberLoanHistory updatedLoanHistory = memberLoanHisRepository.save(loanHistory);

        // then
        assertThat(updatedLoanHistory.isRenew()).isTrue();
        assertThat(updatedLoanHistory.isRenew()).isEqualTo(loanHistory.isRenew());
    }

    @Test
    @DisplayName("도서 반납 날짜 업데이트")
    void updateReturnDate() {
        // given
        MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .build();
        memberLoanHisRepository.save(loanHistory);

        // when
        loanHistory.doReturn();
        MemberLoanHistory updatedLoanHistory = memberLoanHisRepository.save(loanHistory);

        // then
        assertThat(updatedLoanHistory.getReturnedAt()).isNotNull();
        assertThat(updatedLoanHistory.getReturnedAt()).isEqualTo(loanHistory.getReturnedAt());
    }
}