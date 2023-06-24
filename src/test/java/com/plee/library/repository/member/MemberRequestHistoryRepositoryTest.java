package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberRequestHistory;
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
class MemberRequestHistoryRepositoryTest {
    @Autowired
    private MemberRequestHistoryRepository memberReqHisRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookInfoRepository bookInfoRepository;

    Member member;
    BookInfo bookInfo;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member);

        bookInfo = BookInfo.builder()
                .isbn("9788994492032")
                .title("Java의 정석")
                .author("남궁성")
                .publisher("도우출판")
                .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                .description("책 소개입니다")
                .pubDate("20160201")
                .build();
        bookInfoRepository.save(bookInfo);
    }

    @Test
    @DisplayName("새로운 도서 추가 요청")
    void createMemberRequestHistory() {
        // given
        MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .reqReason("업무에 필요합니다.")
                .build();

        // when
        MemberRequestHistory savedMemberReqHis = memberReqHisRepository.save(memberReqHis);

        // then
        assertNotNull(savedMemberReqHis);
        assertEquals(memberReqHis, savedMemberReqHis);
        assertEquals(memberReqHis.getId(), savedMemberReqHis.getId());
        assertEquals(memberReqHis.getReqReason(), savedMemberReqHis.getReqReason());
        assertEquals(memberReqHis.isApproved(), savedMemberReqHis.isApproved());
    }

    @Test
    @DisplayName("추가 요청한 모든 기록 조회")
    void findAllMemberRequestHistory() {
        // given
        BookInfo bookInfo2 = BookInfo.builder()
                .isbn("9788966261208")
                .title("HTTP 완벽 가이드")
                .author("안슈 아가왈")
                .publisher("인사이트")
                .image("https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg")
                .description("책 소개입니다")
                .pubDate("20141215")
                .build();
        bookInfoRepository.save(bookInfo2);

        MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .reqReason("업무에 필요합니다.")
                .build();
        memberReqHisRepository.save(memberReqHis);

        MemberRequestHistory memberReqHis2 = MemberRequestHistory.builder()
                .member(member)
                .bookInfo(bookInfo2)
                .reqReason("업무에 필요합니다.")
                .build();
        memberReqHisRepository.save(memberReqHis2);

        // when
        List<MemberRequestHistory> foundMemberReqHis = memberReqHisRepository.findAllByMember(member);

        // then
        assertNotNull(foundMemberReqHis);
        assertEquals(2, foundMemberReqHis.size());
        assertTrue(foundMemberReqHis.contains(memberReqHis));
        assertTrue(foundMemberReqHis.contains(memberReqHis2));
    }


    @Test
    @DisplayName("도서 추가 요청 승인")
    void approveMemberRequestHistory() {
        // given
        MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .reqReason("업무에 필요합니다.")
                .build();
        memberReqHisRepository.save(memberReqHis);

        // when
        memberReqHis.doApprove();
        MemberRequestHistory updatedMemberReqHis = memberReqHisRepository.save(memberReqHis);

        // then
        assertTrue(updatedMemberReqHis.isApproved());
    }

}