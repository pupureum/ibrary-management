package com.plee.library.service.member;

import com.plee.library.domain.member.Member;
import com.plee.library.dto.member.request.LoginMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.member.request.UpdateMemberRequest;
import com.plee.library.dto.member.response.AllMembersResponse;

import java.util.List;

public interface MemberService {
    void saveMember(SignUpMemberRequest request);

    void updateMemberByAdmin(Long memberId, UpdateMemberRequest request);

    void updateMemberInfo(Long memberId, UpdateMemberRequest request);

    boolean checkLoginIdDuplicate(String loginId);

    Member findByLoginId(String loginId);

    List<AllMembersResponse> findAllMembers();
}
