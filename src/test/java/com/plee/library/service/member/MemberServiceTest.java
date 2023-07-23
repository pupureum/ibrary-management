package com.plee.library.service.member;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.domain.member.MemberRequestHistory;
import com.plee.library.domain.member.Role;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.admin.response.MemberStatusResponse;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.member.response.MemberInfoResponse;
import com.plee.library.repository.book.BookInfoRepository;
import com.plee.library.repository.member.MemberRequestHistoryRepository;
import com.plee.library.util.message.MemberMessage;
import com.plee.library.repository.book.BookRepository;
import com.plee.library.repository.member.MemberLoanHistoryRepository;
import com.plee.library.repository.member.MemberRepository;
import com.plee.library.dto.member.condition.LoanHistorySearchCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberLoanHistoryRepository memberLoanHisRepository;
    @Mock
    private MemberRequestHistoryRepository memberReqHisRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookInfoRepository bookInfoRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @InjectMocks
    private MemberServiceImpl memberService;

    @Nested
    @DisplayName("회원가입 요청의 유효성 검증")
    class ValidateSignupRequest {
        @Test
        @DisplayName("회원가입 성공")
        void validateSignupRequest_Success() {
            // given
            SignUpMemberRequest req = SignUpMemberRequest.builder()
                    .loginId("plee@gmail.com")
                    .password("test")
                    .confirmPassword("test")
                    .name("이푸름")
                    .build();

            BindingResult bindingResult = mock(BindingResult.class);

            // when
            memberService.validateSignupRequest(req, bindingResult);
            // then
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("실패: 비밀번호와 비밀번호 확인이 일치하지 않는 경우")
        void validateSignupRequest_FailPassword() {
            // given
            SignUpMemberRequest req = SignUpMemberRequest.builder()
                    .loginId("plee@gmail.com")
                    .password("test")
                    .confirmPassword("invalid")
                    .name("이푸름")
                    .build();

            BindingResult bindingResult = new BeanPropertyBindingResult(req, "signupRequest");

            // when
            memberService.validateSignupRequest(req, bindingResult);

            // then
            assertThat(bindingResult.hasErrors()).isTrue();
            String expectedMessage = MemberMessage.NOT_MATCHED_PASSWORD.getMessage();
            assertThat(bindingResult.getFieldError("confirmPassword").getDefaultMessage()).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("실패: 아이디가 중복되는 경우")
        void validateSignupRequest_FailDupLoginId() {
            // given
            when(memberRepository.existsByLoginId("plee@gmail.com")).thenReturn(true);

            SignUpMemberRequest req = SignUpMemberRequest.builder()
                    .loginId("plee@gmail.com")
                    .password("test")
                    .confirmPassword("test")
                    .name("이푸름")
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(req, "signupRequest");

            // when
            memberService.validateSignupRequest(req, bindingResult);

            // then
            assertThat(bindingResult.hasErrors()).isTrue();
            String expectedMessage = MemberMessage.DUPLICATE_LOGIN_ID.getMessage();
            assertThat(bindingResult.getFieldError("loginId").getDefaultMessage()).isEqualTo(expectedMessage);
        }
    }

    @Test
    @DisplayName("회원 정보 저장")
    void saveMember() {
        // given
        SignUpMemberRequest req = SignUpMemberRequest.builder()
                .loginId("plee@gmail.com")
                .password("test")
                .name("이푸름")
                .build();

        Member member = Member.builder()
                .loginId(req.getLoginId())
                .password(req.getPassword())
                .name(req.getName())
                .build();

        given(memberRepository.save(any(Member.class))).willReturn(member);

        // when
        memberService.saveMember(req);

        // then
        then(memberRepository).should(times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("특정 회원 정보 조회")
    void findMember() {
        // given
        Member member = Member.builder()
                .loginId("plee@gmail.com")
                .password("test")
                .name("이푸름")
                .role(Role.MEMBER)
                .build();

        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

        // when
        MemberInfoResponse foundMemberResponse = memberService.findMember(1L);

        // then
        MemberInfoResponse expectedMemberResponse = MemberInfoResponse.from(member);

        assertThat(foundMemberResponse).isNotNull();
        assertThat(foundMemberResponse).isInstanceOf(MemberInfoResponse.class);
        assertThat(foundMemberResponse).usingRecursiveComparison().isEqualTo(expectedMemberResponse);
        then(memberRepository).should().findById(anyLong());
        then(memberRepository).should(times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("모든 회원 정보 조회")
    void findAllMembers() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        // 5명의 회원 정보 생성
        List<Member> members = createMembers();

        given(memberRepository.findAll(pageable)).willReturn(new PageImpl<>(members, pageable, members.size()));

        // when
        Page<MemberStatusResponse> foundMembers = memberService.findAllMembers(pageable);

        // then
        Page<MemberStatusResponse> expectedResponse = createAllMemberInfoResponsePage(members, pageable);

        assertThat(foundMembers).isNotNull();
        assertThat(foundMembers.getTotalElements()).isEqualTo(5);
        assertThat(foundMembers).usingRecursiveComparison().isEqualTo(expectedResponse);
        then(memberRepository).should().findAll(pageable);
        then(memberRepository).should(times(1)).findAll(pageable);
    }

    private Page<MemberStatusResponse> createAllMemberInfoResponsePage(List<Member> members, Pageable pageable) {
        List<MemberStatusResponse> response = members.stream()
                .map(member -> MemberStatusResponse.builder()
                        .name(member.getName())
                        .loginId(member.getLoginId())
                        .password(member.getPassword())
                        .role(member.getRole())
                        .createdAt(member.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
        return new PageImpl<>(response, pageable, members.size());
    }

    private List<Member> createMembers() {
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Member member = Member.builder()
                    .loginId("plee@gmail.com")
                    .password("test")
                    .name("이푸름")
                    .role(Role.MEMBER)
                    .build();
            members.add(member);
        }
        return members;
    }

    @Test
    @DisplayName("현재 비밀번호와 일치 여부 확인")
    void checkCurrentPassword() {
        // given
        String password = "password";
        Member member = Member.builder()
                .loginId("plee@gmail.com")
                .password(password)
                .name("이푸름")
                .role(Role.MEMBER)
                .build();

        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

        // when
        boolean isMatched = memberService.checkCurrentPassword(password, 1L);

        // then
        assertThat(isMatched).isTrue();
        then(passwordEncoder).should().matches(anyString(), anyString());
        then(passwordEncoder).should(times(1)).matches(anyString(), anyString());
        then(memberRepository).should().findById(anyLong());
        then(memberRepository).should(times(1)).findById(anyLong());
    }

    @Nested
    @DisplayName("관리자에 의한 회원 정보 수정")
    class UpdateMemberByAdminTest {
        private Member member;

        @BeforeEach
        void setUp() {
            member = Member.builder()
                    .loginId("plee@gmail.com")
                    .password("password")
                    .name("이푸름")
                    .role(Role.MEMBER)
                    .build();
        }

        @Test
        @DisplayName("이름만 변경")
        void updateMemberName() {
            // given
            UpdateMemberRequest req = new UpdateMemberRequest("newName", member.getPassword(), member.getRole());
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when
            memberService.updateMemberByAdmin(1L, req);

            // then
            assertThat(member.getName()).isEqualTo(req.getName());
            assertThat(member.getPassword()).isEqualTo(req.getPassword());
            assertThat(member.getRole()).isEqualTo(req.getRole());
        }

        @Test
        @DisplayName("권한만 변경")
        void updateMemberPassword() {
            // given
            UpdateMemberRequest req = new UpdateMemberRequest(member.getName(), member.getPassword(), Role.ADMIN);
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when
            memberService.updateMemberByAdmin(1L, req);

            // then
            assertThat(member.getName()).isEqualTo(req.getName());
            assertThat(member.getPassword()).isEqualTo(req.getPassword());
            assertThat(member.getRole()).isEqualTo(req.getRole());
        }

        @Test
        @DisplayName("이름, 권한 모두 변경")
        void updateMemberAll() {
            // given
            UpdateMemberRequest req = new UpdateMemberRequest("newName", member.getPassword(), Role.ADMIN);
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when
            memberService.updateMemberByAdmin(1L, req);

            // then
            assertThat(member.getName()).isEqualTo(req.getName());
            assertThat(member.getPassword()).isEqualTo(req.getPassword());
            assertThat(member.getRole()).isEqualTo(req.getRole());
        }
    }

    @Nested
    @DisplayName("회원에 의한 회원 정보 수정")
    class ChangeMemberInfo {
        private Member member;

        @BeforeEach
        void setUp() {
            member = Member.builder()
                    .loginId("plee@gmail.com")
                    .password("password")
                    .name("이푸름")
                    .role(Role.MEMBER)
                    .build();
        }

        @Test
        @DisplayName("이름만 변경")
        void changeMemberName() {
            // given
            UpdateMemberRequest req = new UpdateMemberRequest("newName", "", member.getRole());
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when
            memberService.changeMemberInfo(1L, req);

            // then
            assertThat(member.getName()).isEqualTo(req.getName());
            assertThat(member.getPassword()).isEqualTo(member.getPassword());
        }

        @Test
        @DisplayName("비밀번호만 변경")
        void updateMemberPassword() {
            // given
            String newPassword = "newPassword";
            UpdateMemberRequest req = new UpdateMemberRequest(member.getName(), newPassword, member.getRole());

            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
            given(passwordEncoder.matches(newPassword, member.getPassword())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn(newPassword);

            // when
            memberService.changeMemberInfo(1L, req);
            System.out.println(member.getPassword());
            System.out.println(req.getPassword());

            // then
            assertThat(member.getName()).isEqualTo(member.getName());
            assertThat(member.getPassword()).isEqualTo(req.getPassword());
        }

        @Test
        @DisplayName("이름, 비밀번호 모두 변경")
        void updateMemberAll() {
            // given
            String newPassword = "newPassword";
            UpdateMemberRequest req = new UpdateMemberRequest("newName", newPassword, member.getRole());
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
            given(passwordEncoder.matches(newPassword, member.getPassword())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn(newPassword);

            // when
            memberService.changeMemberInfo(1L, req);

            // then
            assertThat(member.getName()).isEqualTo(req.getName());
            assertThat(member.getPassword()).isEqualTo(req.getPassword());
        }

        @Test
        @DisplayName("실패: 이름과 비밀번호 모두 변경사항이 없는 경우")
        void updateMember_Notchanged() {
            // given
            UpdateMemberRequest req = new UpdateMemberRequest(member.getName(), member.getPassword(), member.getRole());
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
            given(passwordEncoder.matches(member.getPassword(), member.getPassword())).willReturn(true);

            // when, then
            assertThatThrownBy(() -> memberService.changeMemberInfo(1L, req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(MemberMessage.NOT_CHANGED_ANYTHING.getMessage());
        }
    }

    @Nested
    @DisplayName("회원 삭제")
    class DeleteMemberTest {
        Member member;

        @BeforeEach
        void setUp() {
            member = Member.builder()
                    .loginId("plee@gmail.com")
                    .password("password")
                    .name("이푸름")
                    .role(Role.MEMBER)
                    .build();
        }

        @Test
        @DisplayName("대출중인 도서가 없는 경우")
        void deleteMember_NotLoanedBook() {
            // given
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
            // 대출 기록을 빈 리스트로 반환
            given(memberLoanHisRepository.searchHistory(any(LoanHistorySearchCondition.class))).willReturn(Collections.emptyList());
            given(memberReqHisRepository.findByMemberIdAndIsApprovedFalse(anyLong())).willReturn(Collections.emptyList());

            // when
            memberService.deleteMember(1L);

            // then
            then(bookRepository).should(never()).findByBookInfoIsbn(anyString());
            then(memberRepository).should(times(1)).delete(any(Member.class));
        }

        @Test
        @DisplayName("대출중인 도서가 있는 경우")
        void deleteMember_ForceReturnBook() {
            // given
            Book book = Book.builder()
                    .bookInfo(BookInfo.builder()
                            .isbn("1234567891234")
                            .title("title")
                            .build())
                    .quantity(2)
                    .build();
            // 대출 기록 생성 및 대여 가능 도서 수 감소
            List<MemberLoanHistory> memberLoanHistoryList = new ArrayList<>();
            memberLoanHistoryList.add(MemberLoanHistory.builder()
                    .member(member)
                    .bookInfo(book.getBookInfo())
                    .build());
            book.decreaseLoanableCnt();

            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
            given(memberLoanHisRepository.searchHistory(any(LoanHistorySearchCondition.class))).willReturn(memberLoanHistoryList);
            given(bookRepository.findByBookInfoIsbn(anyString())).willReturn(Optional.of(book));
            given(memberReqHisRepository.findByMemberIdAndIsApprovedFalse(anyLong())).willReturn(Collections.emptyList());

            // when
            memberService.deleteMember(1L);

            // then
            then(bookRepository).should().findByBookInfoIsbn(anyString());
            then(bookRepository).should(times(1)).findByBookInfoIsbn(anyString());
            then(memberRepository).should(times(1)).delete(any(Member.class));
            // 도서 수량 증가 확인
            assertThat(book.getLoanableCnt()).isEqualTo(2);
        }

        @Test
        @DisplayName("요청 도서가 입고 처리되지 않았고, 해당 회원만 신청한 도서가 있는 경우")
        void deleteMember_NotReceivedUniq() {
            // given
            BookInfo bookInfo = BookInfo.builder()
                    .isbn("1234567891234")
                    .title("title")
                    .build();

            // 신청 내역 생성
            MemberRequestHistory reqHistory = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .build();

            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
            given(memberLoanHisRepository.searchHistory(any(LoanHistorySearchCondition.class))).willReturn(Collections.emptyList());
            given(memberReqHisRepository.findByMemberIdAndIsApprovedFalse(anyLong())).willReturn(List.of(reqHistory));
            given(memberReqHisRepository.countByBookInfo(bookInfo)).willReturn(1L);

            // when
            memberService.deleteMember(1L);

            // then
            then(bookRepository).should(never()).findByBookInfoIsbn(anyString());
            then(bookInfoRepository).should(times(1)).delete(bookInfo);
            then(memberRepository).should(times(1)).delete(any(Member.class));
        }

        @Test
        @DisplayName("요청 도서가 입고 처리되지 않았고, 다른 회원도 해당 도서를 신청한 경우")
        void deleteMember_NotReceivedNotUniq() {
            // given
            BookInfo bookInfo = BookInfo.builder()
                    .isbn("1234567891234")
                    .title("title")
                    .build();

            // 신청 내역 생성
            MemberRequestHistory reqHistory = MemberRequestHistory.builder()
                    .member(member)
                    .bookInfo(bookInfo)
                    .build();

            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
            given(memberLoanHisRepository.searchHistory(any(LoanHistorySearchCondition.class))).willReturn(Collections.emptyList());
            given(memberReqHisRepository.findByMemberIdAndIsApprovedFalse(anyLong())).willReturn(List.of(reqHistory));
            given(memberReqHisRepository.countByBookInfo(bookInfo)).willReturn(0L);

            // when
            memberService.deleteMember(1L);

            // then
            then(bookRepository).should(never()).findByBookInfoIsbn(anyString());
            then(bookInfoRepository).should(never()).delete(bookInfo);
            then(memberRepository).should(times(1)).delete(any(Member.class));
        }
    }
}