package com.plee.library.config.testUserDetails;

import com.plee.library.config.MemberAdapter;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class TestUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = createTestMember();
        return new MemberAdapter(member);
    }

    private Member createTestMember() {
        return Member.builder()
                .id(1L)
                .name("이푸름")
                .loginId("plee@gmail.com")
                .password("password")
                .role(Role.ADMIN)
                .build();
    }
}
