package com.plee.library.service.member;

import com.plee.library.domain.member.Member;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.member.response.MemberInfoResponse;
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

    @Transactional(readOnly = true)
    public MemberInfoResponse findMember(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .map(m -> MemberInfoResponse.builder()
                        .id(m.getId())
                        .loginId(m.getLoginId())
                        .name(m.getName())
                        .role(m.getRole())
                        .createdAt(m.getCreatedAt().toLocalDate())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("계정이 존재하지 않습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberInfoResponse> findAllMembers() {
        return memberRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(m -> MemberInfoResponse.builder()
                        .id(m.getId())
                        .loginId(m.getLoginId())
                        .name(m.getName())
                        .role(m.getRole())
                        .createdAt(m.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.NOT_FOUND_MEMBER));
        if (!request.getName().equals(member.getName())) {
            member.updateName(request.getName());
        }
        // 새 비밀번호가 없다면 이름만 변경
        if (request.getNewPassword().isEmpty()) {
            return;
        }
        if (!passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        } else {
            throw new CustomException(MemberErrorCode.NOT_CHANGE_PASSWORD);
        }
    }

    @Transactional(readOnly = true)
    public boolean checkCurrentPassword(String currentPassword, String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("계정이 존재하지 않습니다."));
        System.out.println(passwordEncoder.encode(member.getPassword()));
        System.out.println(currentPassword);
        return passwordEncoder.matches(currentPassword, member.getPassword());
    }

    @Transactional(readOnly = true)
    public boolean checkLoginIdDuplicate(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Member findByLoginId(String loginId) {
//        return memberRepository.findByLoginId(loginId)
//                .orElseThrow(() -> new UsernameNotFoundException("계정이 존재하지 않습니다. 회원가입 진행 후 로그인 해주세요."));
//    }


    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("계정이 존재하지 않습니다."));
        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }
}
