package com.plee.library.service.member;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.member.Member;
import com.plee.library.config.MemberAdapter;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.member.response.MemberInfoResponse;
import com.plee.library.exception.message.MemberError;
import com.plee.library.repository.book.BookRepository;
import com.plee.library.repository.member.MemberLoanHistoryRepository;
import com.plee.library.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService, UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberLoanHistoryRepository memberLoanHisRepository;
    private final BookRepository bookRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void saveMember(SignUpMemberRequest request) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        memberRepository.save(Member.builder()
                .loginId(request.getLoginId())
                .password(encoder.encode(request.getPassword()))
                .name(request.getName())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateSignupRequest(SignUpMemberRequest request, BindingResult bindingResult) {
        // 비밀번호 입력 2개가 일치하는지 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.reject("passwordNotMatch", MemberError.NOT_MATCHED_PASSWORD.getMessage());
        }
        // 로그인 아이디가 중복되는지 확인
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            bindingResult.reject("duplicateLoginId", MemberError.DUPLICATE_LOGIN_ID.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .map(m -> MemberInfoResponse.builder()
                        .id(m.getId())
                        .loginId(m.getLoginId())
                        .name(m.getName())
                        .role(m.getRole())
                        .createdAt(m.getCreatedAt().toLocalDate())
                        .build())
                .orElseThrow(() -> new NoSuchElementException(MemberError.NOT_FOUND_MEMBER.getMessage()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberInfoResponse> findAllMembers(Pageable pageable) {
        // 회원들을 최신순으로 Pagination 하여 조회
        Page<Member> members = memberRepository.findAll(pageable);

        // 조회된 회원들을 MemberInfoResponse 객체로 변환
        List<MemberInfoResponse> response = members.stream()
                .map(m -> MemberInfoResponse.builder()
                        .id(m.getId())
                        .loginId(m.getLoginId())
                        .name(m.getName())
                        .role(m.getRole())
                        .createdAt(m.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
        return new PageImpl<>(response, pageable, members.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkCurrentPassword(String currentPassword, Long memberId) {
        Member member =findMemberById(memberId);
        return passwordEncoder.matches(currentPassword, member.getPassword());
    }

    @Override
    @Transactional
    public void updateMemberByAdmin(Long memberId, UpdateMemberRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberError.NOT_FOUND_MEMBER.getMessage()));

        // 이름, 권한이 변경되었다면 변경
        if (!request.getName().equals(member.getName())) {
            member.changeName(request.getName());
        }
        if (!request.getRole().equals(member.getRole())) {
            member.changeRole(request.getRole());
        }
    }

    @Override
    @Transactional
    public void changeMemberInfo(Long memberId, UpdateMemberRequest request) {
        Member member = findMemberById(memberId);
        String newName = request.getName();
        String newPassword = request.getNewPassword();

        // 이름이 변경되었다면 변경
        if (!newName.equals(member.getName())) {
            member.changeName(newName);
        }
        // 새 비밀번호가 없다면 이름만 변경
        if (request.getNewPassword().isEmpty()) {
            return;
        }
        // 새 비밀번호가 기존 비밀번호와 다르다면 변경
        if (!passwordEncoder.matches(newPassword, member.getPassword())) {
            member.changePassword(passwordEncoder.encode(newPassword));
        } else {
            throw new IllegalStateException(MemberError.NOT_CHANGED_PASSWORD.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = findMemberById(memberId);

        // 대출중인 도서가 있는 회원의 경우 강제 반납 처리
        List<MemberLoanHistory> notReturnedHistories = memberLoanHisRepository.findByMemberIdAndReturnedAtIsNull(memberId);
        notReturnedHistories.forEach(history -> {
            history.doReturn();
            bookRepository.findByBookInfoIsbn(history.getBookInfo().getIsbn())
                    .ifPresent(Book::increaseLoanableCnt);
        });

        memberRepository.delete(member);
        log.info("SUCCESS delete member id : {}", memberId);
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(MemberError.NOT_FOUND_MEMBER.getMessage()));
        return new MemberAdapter(member);
    }

    @Transactional(readOnly = true)
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberError.NOT_FOUND_MEMBER.getMessage()));
    }
}
