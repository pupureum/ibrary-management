package com.plee.library.service.member;

import com.plee.library.domain.member.Member;
import com.plee.library.dto.member.request.LoginMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.exception.CustomException;
import com.plee.library.exception.code.MemberErrorCode;
import com.plee.library.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService, UserDetailsService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Long saveMember(SignUpMemberRequest request) {
        if (checkLoginIdDuplicate(request.getLoginId())) {
            return null;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return memberRepository.save(Member.builder()
                .loginId(request.getLoginId())
                .password(encoder.encode(request.getPassword()))
                .name(request.getName())
                .build()).getId();
    }

    public Member login(LoginMemberRequest request) {
        log.info("login member: {}", request.getLoginId());
        return memberRepository.findByLoginId(request.getLoginId())
                .filter(member -> passwordEncoder.matches(member.getPassword(),request.getPassword()))
                .orElse(null);
    }

    public boolean checkLoginIdDuplicate(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    @Override
    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() ->  new UsernameNotFoundException("계정이 존재하지 않습니다. 회원가입 진행 후 로그인 해주세요."));
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = findByLoginId(loginId);
        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }
}
