package com.plee.library.repository.member;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestJPAConfig.class)
@DisplayName("MemberRepository 테스트")
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("회원 생성 테스트")
    public class saveMemberTest {
        @Test
        @DisplayName("성공 테스트")
        void signUpMember() {
            // given
            Member member = Member.builder()
                    .name("이푸름")
                    .loginId("plee@gmail.com")
                    .password("test1234")
                    .build();

            // when
            Member savedMember = memberRepository.save(member);

            // then
            assertThat(savedMember).isNotNull();
            assertThat(savedMember).isEqualTo(member);
            assertThat(savedMember.getId()).isEqualTo(member.getId());
            assertThat(savedMember.getName()).isEqualTo(member.getName());
            assertThat(savedMember.getLoginId()).isEqualTo(member.getLoginId());
            assertThat(savedMember.getPassword()).isEqualTo(member.getPassword());
            assertThat(savedMember.getRole()).isEqualTo(member.getRole());
            assertThat(savedMember.getMemberLoanHistories())
                    .hasSize(member.getMemberLoanHistories().size());
            assertThat(savedMember.getMemberRequestHistories())
                    .hasSize(member.getMemberRequestHistories().size());
            assertThat(savedMember.getMemberBookmarks())
                    .hasSize(member.getMemberBookmarks().size());
        }

        @Test
        @DisplayName("실패 테스트: 이미 존재하는 loginId")
        void saveFailWithNotUniqueLoginId() {
            // given
            Member member = Member.builder()
                    .name("이푸름")
                    .loginId("plee@gmail.com")
                    .password("test1234")
                    .build();
            memberRepository.save(member);
            Member member2 = Member.builder()
                    .name("이푸름")
                    .loginId("plee@gmail.com")
                    .password("test12345")
                    .build();

            // when, then
            assertThatThrownBy(() -> memberRepository.save(member2))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("특정 회원 조회 테스트")
    public class findMemberByLoginIdTest {
        @Test
        @DisplayName("loginId로 회원 조회")
        void findMemberByLoginId() {
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
            Member foundMember = memberRepository.findByLoginId(member1.getLoginId())
                    .orElse(null);

            // then
            assertThat(foundMember).isNotNull();
            assertThat(foundMember).isEqualTo(member1);
            assertThat(foundMember.getId()).isEqualTo(member1.getId());
            assertThat(foundMember.getName()).isEqualTo(member1.getName());
            assertThat(foundMember.getLoginId()).isEqualTo(member1.getLoginId());
            assertThat(foundMember.getPassword()).isEqualTo(member1.getPassword());
            assertThat(foundMember.getRole()).isEqualTo(member1.getRole());
            assertThat(foundMember.getMemberLoanHistories())
                    .hasSize(member1.getMemberLoanHistories().size())
                    .containsAll(member1.getMemberLoanHistories());
            assertThat(foundMember.getMemberRequestHistories())
                    .hasSize(member1.getMemberRequestHistories().size())
                    .containsAll(member1.getMemberRequestHistories());
        }

        @Test
        @DisplayName("loginId로 존재 여부 확인")
        void findFailWithNotExistLoginId() {
            // given
            String loginId = "no@gmail.com";

            // when
            Boolean result = memberRepository.existsByLoginId(loginId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("전체 회원 조회 테스트")
    void findAllMembers() {
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
        List<Member> foundMembers = memberRepository.findAll();

        // then
        assertThat(foundMembers).size().isEqualTo(2);
        assertThat(foundMembers).contains(member1, member2);
    }

    @Nested
    @DisplayName("회원 정보 수정 테스트")
    public class updateMemberTest {
        @Test
        @DisplayName("회원 권한 변경")
        void changeMemberRole() {
            // given
            Member member = Member.builder()
                    .name("이푸름")
                    .loginId("plee@gmail.com")
                    .password("test1234")
                    .build();
            memberRepository.save(member);

            // when
            member.changeRole(Role.ADMIN);
            Member updatedMember = memberRepository.save(member);

            // then
            assertThat(updatedMember.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("회원 이름 수정")
        void changeMemberName() {
            // given
            Member member = Member.builder()
                    .name("이푸름")
                    .loginId("plee@gmail.com")
                    .password("test1234")
                    .build();
            memberRepository.save(member);

            // when
            String newName = "이푸름름";
            member.changeName(newName);
            Member updatedMember = memberRepository.save(member);

            // then
            assertThat(updatedMember.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("회원 비밀번호 수정")
        void changeMemberPwd() {
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
            Member updatedMember = memberRepository.save(member);

            // then
            assertThat(updatedMember.getPassword()).isEqualTo(newPwd);
        }
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    void deleteMember() {
        // given
        Member member = Member.builder()
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("test1234")
                .build();
        memberRepository.save(member);

        // when
        memberRepository.delete(member);

        // then
        assertThat(memberRepository.existsById(member.getId())).isFalse();
    }
}