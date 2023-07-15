package com.plee.library.service.member;

import com.plee.library.domain.member.Member;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.admin.response.MemberInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

public interface MemberService {
    Member saveMember(SignUpMemberRequest request);

    void validateSignupRequest(SignUpMemberRequest request, BindingResult bindingResult);

    com.plee.library.dto.member.response.MemberInfoResponse findMember(Long memberId);

    Page<MemberInfoResponse> findAllMembers(Pageable pageable);

    boolean checkCurrentPassword(String currentPassword, Long memberId);

    void updateMemberByAdmin(Long memberId, UpdateMemberRequest request);

    void changeMemberInfo(Long memberId, UpdateMemberRequest request);

    void deleteMember(Long memberId);
}
