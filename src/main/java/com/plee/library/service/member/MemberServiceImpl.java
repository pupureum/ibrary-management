package com.plee.library.service.member;

import com.plee.library.domain.member.Member;
import com.plee.library.dto.admin.response.AllBookRequestResponse;
import com.plee.library.dto.member.request.LoginMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.member.request.UpdateMemberRequest;
import com.plee.library.dto.member.response.AllMembersResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService, UserDetailsService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void saveMember(SignUpMemberRequest request) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        memberRepository.save(Member.builder()
                .loginId(request.getLoginId())
                .password(encoder.encode(request.getPassword()))
                .name(request.getName())
                .build());
    }

    @Override
    @Transactional
    public void updateMemberByAdmin(Long memberId, UpdateMemberRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.NOT_FOUND_MEMBER));
        if (!request.getName().equals(member.getName())) {
            member.updateName(request.getName());
        }
        if (!request.getRole().equals(member.getRole())) {
            member.updateRole(request.getRole());
        }
    }

    @Override
    @Transactional
    public void updateMemberInfo(Long memberId, UpdateMemberRequest request) {

    }

    @Transactional(readOnly = true)
    public boolean checkLoginIdDuplicate(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    @Override
    @Transactional(readOnly = true)
    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("계정이 존재하지 않습니다. 회원가입 진행 후 로그인 해주세요."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllMembersResponse> findAllMembers() {
        return memberRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(m -> AllMembersResponse.builder()
                        .id(m.getId())
                        .loginId(m.getLoginId())
                        .name(m.getName())
                        .role(m.getRole())
                        .createdAt(m.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
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
