package com.plee.library.repository.member;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberRequestHistory;
import com.plee.library.repository.book.BookInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestJPAConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("MemberLoanHistoryRepository 테스트")
class MemberRequestHistoryRepositoryTest {
    @Autowired
    private MemberRequestHistoryRepository memberReqHisRepository;
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
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();
        member = memberRepository.save(member);

        bookInfo = BookInfo.builder()
                .isbn("9788994492032")
                .title("Java의 정석")
                .author("남궁성")
                .publisher("도우출판")
                .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                .description("책 소개입니다")
                .pubDate("20160201")
                .build();
        bookInfo = bookInfoRepository.save(bookInfo);
    }

    @Nested
    @DisplayName("도서 추가 요청 생성 테스트")
    public class SaveBookRequestTest {
        @Test
        @DisplayName("성공 테스트")
        void save() {
            // given
            MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .requestReason("업무에 필요합니다.")
                    .build();

            // when
            MemberRequestHistory savedMemberReqHis = memberReqHisRepository.save(memberReqHis);

            // then
            assertThat(savedMemberReqHis).isNotNull();
            assertThat(savedMemberReqHis).isEqualTo(memberReqHis);
            assertThat(savedMemberReqHis.getId()).isEqualTo(memberReqHis.getId());
            assertThat(savedMemberReqHis.getRequestReason()).isEqualTo(memberReqHis.getRequestReason());
            assertThat(savedMemberReqHis.isApproved()).isEqualTo(memberReqHis.isApproved());
        }

        @Test
        @DisplayName("실패 테스트: 신청 사유가 null인 경우")
        void saveWithoutRequestReason() {
            // given
            MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .requestReason(null)
                    .build();

            // when, then
            assertThatThrownBy(() -> memberReqHisRepository.save(memberReqHis))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("특정 추가 요청 존재 여부 확인 테스트")
    public class findMemberRequestHistoryTest {
        @Test
        @DisplayName("회원 ID와 도서 정보 ISBN으로 조회")
        void existsByMemberIdAndBookInfoIsbnTest() {
            // given
            MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .requestReason("업무에 필요합니다.")
                    .build();
            memberReqHisRepository.save(memberReqHis);

            // when
            boolean exists = memberReqHisRepository.existsByMemberIdAndBookInfoIsbn(member.getId(), bookInfo.getIsbn());

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("특정 도서 요청 중 승인되지 않은 요청이 존재하는지 확인")
        void existsByBookInfoIsbnAndApprovedFalseTest() {
            // given
            MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .requestReason("업무에 필요합니다.")
                    .build();
            memberReqHisRepository.save(memberReqHis);
            memberReqHis.doApprove();

            // when
            boolean exists = memberReqHisRepository.existsByBookInfoIsbnAndIsApprovedFalse(bookInfo.getIsbn());

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("모든 기록 조회 테스트")
    public class FindAllMemberRequestHistoryTest {

        MemberRequestHistory memberReqHis1;
        MemberRequestHistory memberReqHis2;
        MemberRequestHistory memberReqHis3;

        @BeforeEach
        void setUp() {
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

            memberReqHis1 = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .requestReason("업무에 필요합니다.")
                    .build();
            memberReqHisRepository.save(memberReqHis1);

            memberReqHis2 = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo2)
                    .requestReason("업무에 필요합니다.")
                    .build();
            memberReqHisRepository.save(memberReqHis2);

            Member member2 = Member.builder()
                    .name("테스트")
                    .loginId("test@gmail.com")
                    .password("test1234")
                    .build();
            memberRepository.save(member2);

            memberReqHis3 = MemberRequestHistory.builder()
                    .member(member2)
                    .bookInfo(bookInfo2)
                    .requestReason("업무에 필요합니다.")
                    .build();
            memberReqHisRepository.save(memberReqHis3);
        }

        @Test
        @DisplayName("특정 회원의 모든 기록 조회")
        void findAllRequestHistoryByMemberTest() {
            // given

            // when
            Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
            Page<MemberRequestHistory> foundMemberReqHis = memberReqHisRepository.findAllByMemberId(member.getId(), pageable);

            // then
            assertThat(foundMemberReqHis).isNotNull();
            assertThat(foundMemberReqHis.getTotalElements()).isEqualTo(2);
            assertThat(foundMemberReqHis.toList()).contains(memberReqHis1, memberReqHis2);
        }

        @Test
        @DisplayName("모든 기록 조회")
        void findAllMemberRequestHistoryTest() {
            // given

            // when
            Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
            Page<MemberRequestHistory> foundMemberReqHis = memberReqHisRepository.findAll(pageable);

            // then
            assertThat(foundMemberReqHis).isNotNull();
            assertThat(foundMemberReqHis.getTotalElements()).isEqualTo(3);
            assertThat(foundMemberReqHis.toList()).contains(memberReqHis1, memberReqHis2, memberReqHis3);
        }

    }

    @Nested
    @DisplayName("도서 추가 요청 승인 테스트")
    public class ApproveMemberRequestHistoryTest {
        @Test
        @DisplayName("하나의 요청 승인")
        void approveMemberRequestHistoryTest() {
            // given
            MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .requestReason("업무에 필요합니다.")
                    .build();
            memberReqHisRepository.save(memberReqHis);

            // when
            memberReqHis.doApprove();
            MemberRequestHistory updatedMemberReqHis = memberReqHisRepository.save(memberReqHis);

            // then
            assertThat(updatedMemberReqHis.isApproved()).isTrue();
        }

        @Test
        @DisplayName("특정 도서를 요청한 모든 기록 승인")
        void approveByBookInfoIsbn() {
            // given
            MemberRequestHistory memberReqHis1 = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .requestReason("업무에 필요합니다.")
                    .build();
            memberReqHisRepository.save(memberReqHis1);

            Member member2 = Member.builder()
                    .name("테스트")
                    .loginId("test@gmail.com")
                    .password("test1234")
                    .build();
            memberRepository.save(member2);

            MemberRequestHistory memberReqHis2 = MemberRequestHistory.builder()
                    .member(member2)
                    .bookInfo(bookInfo)
                    .requestReason("업무에 필요합니다.")
                    .build();
            memberReqHisRepository.save(memberReqHis2);

            // when
            memberReqHisRepository.approveByBookInfoIsbn(bookInfo.getIsbn());

            // then
            assertThat(memberReqHisRepository.findById(memberReqHis1.getId()).orElse(null).isApproved()).isTrue();
            assertThat(memberReqHisRepository.findById(memberReqHis2.getId()).orElse(null).isApproved()).isTrue();
        }
    }

    @Test
    @DisplayName("도서 요청 기록 삭제 테스트")
    void deleteTest() {
        // given
        MemberRequestHistory memberReqHis = MemberRequestHistory.builder()
                .member(member)
                .bookInfo(bookInfo)
                .requestReason("업무에 필요합니다.")
                .build();
        memberReqHisRepository.save(memberReqHis);

        // when
        memberReqHisRepository.delete(memberReqHis);

        // then
        assertThat(memberReqHisRepository.existsById(memberReqHis.getId())).isFalse();
    }

}