package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.repository.book.BookInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("MemberLoanHistoryRepository 테스트")
class MemberLoanHistoryRepositoryTest {
    @Autowired
    private MemberLoanHistoryRepository memberLoanHisRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookInfoRepository bookInfoRepository;

    Member member;
    BookInfo bookInfo1;
    BookInfo bookInfo2;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member);

        bookInfo1 = BookInfo.builder()
                .isbn("9788994492032")
                .title("Java의 정석")
                .author("남궁성")
                .publisher("도우출판")
                .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                .description("책 소개입니다")
                .pubDate("20160201")
                .build();
        bookInfoRepository.save(bookInfo1);

        bookInfo2 = BookInfo.builder()
                .isbn("9788966261208")
                .title("HTTP 완벽 가이드")
                .author("안슈 아가왈")
                .publisher("인사이트")
                .image("https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg")
                .description("책 소개입니다")
                .pubDate("20141215")
                .build();
        bookInfoRepository.save(bookInfo2);

    }

    @Test
    @DisplayName("회원 대출 내역 생성")
    void createMemberLoanHistory() {
        // given
        MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo1)
                .build();

        // when
        MemberLoanHistory savedLoanHistory = memberLoanHisRepository.save(loanHistory);

        // then
        assertNotNull(savedLoanHistory);
        assertEquals(loanHistory.getId(), savedLoanHistory.getId());
        assertEquals(loanHistory.isRenew(), savedLoanHistory.isRenew());
        assertEquals(loanHistory.getReturnDate(), savedLoanHistory.getReturnDate());
    }

    @Test
    @DisplayName("회원의 대출 내역 조회")
    void findLoanHistoryByMember() {
        // given
        MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo1)
                .build();
        memberLoanHisRepository.save(loanHistory);
        MemberLoanHistory loanHistory2 = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo2)
                .build();
        memberLoanHisRepository.save(loanHistory2);

        // when
        List<MemberLoanHistory> foundLoanHistories = memberLoanHisRepository.findAllByMember(member);

        // then
        assertNotNull(foundLoanHistories);
        assertEquals(2, foundLoanHistories.size());
        assertTrue(foundLoanHistories.contains(loanHistory));
        assertTrue(foundLoanHistories.contains(loanHistory2));
    }

    @Test
    @DisplayName("대출 연장 상태 업데이트")
    void doRenew() {
        // given
        MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo1)
                .build();
        memberLoanHisRepository.save(loanHistory);

        // when
        loanHistory.doRenew();
        MemberLoanHistory updatedLoanHistory = memberLoanHisRepository.save(loanHistory);

        // then
        assertEquals(true, updatedLoanHistory.isRenew());
        assertEquals(loanHistory.getReturnDate(), updatedLoanHistory.getReturnDate());
    }

    @Test
    @DisplayName("도서 반납 날짜 업데이트")
    void updateReturnDate() {
        // given
        MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo1)
                .build();
        memberLoanHisRepository.save(loanHistory);

        // when
        loanHistory.doReturn();
        MemberLoanHistory updatedLoanHistory = memberLoanHisRepository.save(loanHistory);

        // then
        assertNotNull(loanHistory.getReturnDate());
    }

    @Test
    @DisplayName("회원의 대출 내역 삭제")
    void deleteLoanHistoryByMember() {
        // given
        MemberLoanHistory loanHistory = MemberLoanHistory.builder()
                .member(member)
                .bookInfo(bookInfo1)
                .build();
        memberLoanHisRepository.save(loanHistory);

        // when
        memberLoanHisRepository.delete(loanHistory);

        // then
        assertFalse(memberLoanHisRepository.existsById(loanHistory.getId()));
    }

}