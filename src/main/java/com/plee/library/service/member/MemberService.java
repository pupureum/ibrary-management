package com.plee.library.service.member;

import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.member.response.MemberInfoResponse;

import java.util.List;

public interface MemberService {
    void saveMember(SignUpMemberRequest request);

    void updateMemberByAdmin(Long memberId, UpdateMemberRequest request);

    void updateMemberInfo(Long memberId, UpdateMemberRequest request);

    boolean checkLoginIdDuplicate(String loginId);

    boolean checkCurrentPassword(String currentPassword, String loginId);

    MemberInfoResponse findMember(String loginId);

    List<MemberInfoResponse> findAllMembers();
}
