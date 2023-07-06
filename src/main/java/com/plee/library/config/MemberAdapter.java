package com.plee.library.config;

import com.plee.library.domain.member.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.List;

@Getter
public class MemberAdapter extends User {
    private Member member;

    public MemberAdapter(Member member) {
        super(member.getLoginId(), member.getPassword(), getAuthorities(member));
        this.member = member;
    }

    private static List<GrantedAuthority> getAuthorities(Member member) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().toString()));
    }
}