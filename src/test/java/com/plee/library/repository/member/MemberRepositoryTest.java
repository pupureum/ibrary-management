package com.plee.library.repository.member;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.Role;
import com.plee.library.dto.book.request.AddBookRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("MemberRepository 테스트")
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 생성 테스트")
    public void signUpMember() {
        // given
        Member member = Member.builder()
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertNotNull(savedMember);
        assertEquals(member.getId(), savedMember.getId());
        assertEquals(member.getName(), savedMember.getName());
        assertEquals(member.getLoginId(), savedMember.getLoginId());
        assertEquals(member.getPassword(), savedMember.getPassword());
        assertEquals(member.getRole(), savedMember.getRole());
        // TODO 확인하기
        Assertions.assertThat(savedMember.getMemberLoanHistories())
                .hasSize(member.getMemberLoanHistories().size())
                .containsAll(member.getMemberLoanHistories());
        Assertions.assertThat(savedMember.getMemberRequestHistories())
                .hasSize(member.getMemberRequestHistories().size())
                .containsAll(member.getMemberRequestHistories());
    }

    @Test
    @DisplayName("loginId로 회원 조회 테스트")
    public void findMemberByLoginId() {
        // given
        Member member1 = Member.builder()
                .name("test")
                .loginId("test1@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member1);

        Member member2 = Member.builder()
                .name("test2")
                .loginId("test2@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member1);

        // when
        Member findMember = memberRepository.findByLoginId(member1.getLoginId())
                .orElse(null);

        // then
        assertNotNull(findMember);
        assertEquals(member1.getId(), findMember.getId());
        assertEquals(member1.getName(), findMember.getName());
        assertEquals(member1.getLoginId(), findMember.getLoginId());
        assertEquals(member1.getPassword(), findMember.getPassword());
        assertEquals(member1.getRole(), findMember.getRole());
        // TODO 확인하기
        Assertions.assertThat(findMember.getMemberLoanHistories())
                .hasSize(member1.getMemberLoanHistories().size())
                .containsAll(member1.getMemberLoanHistories());
        Assertions.assertThat(findMember.getMemberRequestHistories())
                .hasSize(member1.getMemberRequestHistories().size())
                .containsAll(member1.getMemberRequestHistories());
    }

    @Test
    @DisplayName("전체 회원 조회 테스트")
    public void findAllMembers() {
        // given
        Member member1 = Member.builder()
                .name("test")
                .loginId("test1@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member1);

        Member member2 = Member.builder()
                .name("test2")
                .loginId("test2@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member2);

        // when
        List<Member> findMembers = memberRepository.findAll();

        // then
        assertEquals(2, findMembers.size());
        assertTrue(findMembers.contains(member1));
        assertTrue(findMembers.contains(member2));
    }


    @Test
    @DisplayName("회원 권한 변경 테스트")
    public void changeMemberRole() {
        // given
        Member member = Member.builder()
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member);

        // when
        member.changeRole(Role.Admin);
        Member changedMember = memberRepository.save(member);

        // then
        assertEquals(Role.Admin, changedMember.getRole());
    }

    @Test
    @DisplayName("회원 이름 수정 테스트")
    public void changeMemberName() {
        // given
        Member member = Member.builder()
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member);

        // when
        String newName = "이푸름2";
        member.changeName(newName);
        Member changedMember = memberRepository.save(member);

        // then
        assertEquals(newName, changedMember.getName());
    }

    @Test
    @DisplayName("회원 비밀번호 수정 테스트")
    public void changeMemberPwd() {
        // given
        Member member = Member.builder()
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member);

        // when
        String newPwd = "testtest";
        member.changePassword(newPwd);
        Member changedMember = memberRepository.save(member);

        // then
        assertEquals(newPwd, changedMember.getPassword());
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    public void deleteMember() {
        // given
        Member member = Member.builder()
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member);

        // when
        memberRepository.delete(member);
        Member findMember = memberRepository.findByLoginId(member.getLoginId())
                .orElse(null);

        // then
        assertNull(findMember);
    }
}