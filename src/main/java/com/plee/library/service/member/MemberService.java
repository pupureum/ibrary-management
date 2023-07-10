package com.plee.library.service.member;

import com.plee.library.domain.member.Member;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.admin.response.AllMemberInfoResponse;
import com.plee.library.dto.member.response.MemberInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

public interface MemberService {
    void saveMember(SignUpMemberRequest request);

    void validateSignupRequest(SignUpMemberRequest request, BindingResult bindingResult);

    MemberInfoResponse findMember(Long memberId);

    Page<AllMemberInfoResponse> findAllMembers(Pageable pageable);

    boolean checkCurrentPassword(String currentPassword, Member member);

    void updateMemberByAdmin(Long memberId, UpdateMemberRequest request);

    void changeMemberInfo(Long memberId, UpdateMemberRequest request);

    void deleteMember(Long memberId);
}
