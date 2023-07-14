package com.plee.library.service.member;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.member.Member;
import com.plee.library.config.MemberAdapter;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.admin.response.AllMemberInfoResponse;
import com.plee.library.dto.member.response.MemberInfoResponse;
import com.plee.library.util.message.MemberMsg;
import com.plee.library.repository.book.BookRepository;
import com.plee.library.repository.member.MemberLoanHistoryRepository;
import com.plee.library.repository.member.MemberRepository;
import com.plee.library.dto.member.condition.LoanHistorySearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService, UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberLoanHistoryRepository memberLoanHisRepository;
    private final BookRepository bookRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 회원가입 요청의 유효성을 검증합니다.
     * 비밀번호와 확인 비밀번호가 일치하는지, 로그인 아이디가 중복되는지를 확인하여 BindingResult에 오류를 추가합니다.
     *
     * @param request       회원가입 요청 객체 (SignUpMemberRequest)
     * @param bindingResult 검증 결과를 담는 BindingResult 객체
     */
    @Override
    @Transactional(readOnly = true)
    public void validateSignupRequest(SignUpMemberRequest request, BindingResult bindingResult) {
        // 비밀번호 입력 2개가 일치하는지 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "passwordNotMatch", MemberMsg.NOT_MATCHED_PASSWORD.getMessage());
        }
        // 로그인 아이디가 중복되는지 확인
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            bindingResult.rejectValue("loginId", "duplicateLoginId", MemberMsg.DUPLICATE_LOGIN_ID.getMessage());
        }
    }

    /**
     * 회원 정보를 저장합니다.
     *
     * @param request 회원가입 요청 객체 (SignUpMemberRequest)
     * @return 저장된 회원 정보 (Member 객체)
     */
    @Override
    @Transactional
    public Member saveMember(SignUpMemberRequest request) {
        // 비밀번호 암호화
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 회원 정보 생성
        Member member = Member.builder()
                .loginId(request.getLoginId())
                .password(encoder.encode(request.getPassword()))
                .name(request.getName())
                .build();
        return memberRepository.save(member);
    }

    /**
     * 스프링 시큐리티에서 사용자 정보를 가져옵니다.
     * 로그인 아이디로 회원인지 판별합니다.
     *
     * @param loginId 로그인 ID
     * @return 사용자를 나타내는 UserDetails 객체
     * @throws UsernameNotFoundException 회원 정보를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    @Primary
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(MemberMsg.NOT_FOUND_MEMBER.getMessage()));
        return new MemberAdapter(member);
    }

    /**
     * 특정 회원의 정보를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 조회된 회원의 정보 (MemberInfoResponse 객체)
     * @throws NoSuchElementException 요청한 회원이 존재하지 않을 경우
     */
    @Override
    @Transactional(readOnly = true)
    public MemberInfoResponse findMember(Long memberId) {
        Member foundMember = findMemberById(memberId);

        // 회원 정보를 MemberInfoResponse 객체로 변환
        return MemberInfoResponse.from(foundMember);
    }

    /**
     * 특정 회원을 ID로 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 조회된 Member 객체
     * @throws NoSuchElementException 요청한 회원이 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberMsg.NOT_FOUND_MEMBER.getMessage()));
    }

    /**
     * 모든 회원을 페이지네이션하여 조회합니다.
     *
     * @param pageable 페이지 정보
     * @return 회원 정보를 담은 Page 객체
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AllMemberInfoResponse> findAllMembers(Pageable pageable) {
        // 회원들을 최신순으로 Pagination 하여 조회
        Page<Member> members = memberRepository.findAll(pageable);

        // 조회된 회원들을 MemberInfoResponse 객체 리스트로 변환
        List<AllMemberInfoResponse> response = AllMemberInfoResponse.from(members);
        return new PageImpl<>(response, pageable, members.getTotalElements());
    }

    /**
     * 현재 비밀번호와 일치 여부를 판별합니다.
     *
     * @param currentPassword 입력된 현재 비밀번호
     * @param memberId        회원 ID
     * @return 비밀번호가 일치하면 true, 일치하지 않으면 false
     * @throws NoSuchElementException 회원이 존재하지 않을 경우
     */
    @Override
    @Transactional(readOnly = true)
    public boolean checkCurrentPassword(String currentPassword, Long memberId) {
        Member member = findMemberById(memberId);
        return passwordEncoder.matches(currentPassword, member.getPassword());
    }

    /**
     * 관리자에 의해 회원 정보를 변경합니다.
     * 이름과 권한을 변경할 수 있습니다.
     *
     * @param memberId 회원 ID
     * @param request  변경할 회원 정보
     * @throws NoSuchElementException 요청한 회원이 존재하지 않을 경우
     */
    @Override
    @Transactional
    public void updateMemberByAdmin(Long memberId, UpdateMemberRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberMsg.NOT_FOUND_MEMBER.getMessage()));

        // 이름이 변경되었다면 변경
        if (!request.getName().equals(member.getName())) {
            member.changeName(request.getName());
        }
        // 권한이 변경되었다면 변경
        if (!request.getRole().equals(member.getRole())) {
            member.changeRole(request.getRole());
        }
    }

    /**
     * 회원 정보를 변경합니다.
     * 이름과 비밀번호를 변경할 수 있습니다.
     *
     * @param memberId 회원 ID
     * @param request  변경할 회원 정보
     * @throws NoSuchElementException 요청한 회원이 존재하지 않을 경우
     * @throws IllegalStateException  새 비밀번호가 기존 비밀번호와 동일한 경우
     */
    @Override
    @Transactional
    public void changeMemberInfo(Long memberId, UpdateMemberRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberMsg.NOT_FOUND_MEMBER.getMessage()));
        String newName = request.getName();
        String newPassword = request.getPassword();

        System.out.println("newName = " + newName);
        System.out.println("newPassword = " + newPassword);
        // 이름이 변경되었다면 변경
        if (!newName.equals(member.getName())) {
            member.changeName(newName);
        }
        // 새 비밀번호가 없다면 이름만 변경
        if (request.getPassword().isEmpty()) {
            return;
        }
        // 새 비밀번호가 기존 비밀번호와 다르다면 변경
        if (!passwordEncoder.matches(newPassword, member.getPassword())) {
            member.changePassword(passwordEncoder.encode(newPassword));
        } else {
            throw new IllegalStateException(MemberMsg.NOT_CHANGED_PASSWORD.getMessage());
        }
    }

    /**
     * 특정 회원을 삭제합니다.
     * 대출중인 도서가 있다면 강제 반납 처리합니다.
     *
     * @param memberId 회원 ID
     * @throws NoSuchElementException 요청한 회원이 존재하지 않을 경우
     */
    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = findMemberById(memberId);
        // 대출중인 도서가 있는 회원의 경우 강제 반납 처리
        List<MemberLoanHistory> notReturnedHistories = memberLoanHisRepository.searchHistory(
                LoanHistorySearchCondition
                        .builder()
                        .memberId(memberId)
                        .notReturned(true)
                        .build());

        notReturnedHistories.forEach(history -> {
            history.doReturn();
            bookRepository.findByBookInfoIsbn(history.getBookInfo().getIsbn())
                    .ifPresent(Book::increaseLoanableCnt);
        });

        memberRepository.delete(member);
        log.info("SUCCESS delete member id : {}", memberId);
    }
}
