package com.plee.library.service.member;

import com.plee.library.domain.member.Member;
import com.plee.library.dto.member.request.LoginMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;

public interface MemberService {
    public Long saveMember(SignUpMemberRequest request);

    public Member login(LoginMemberRequest request);

    public boolean checkLoginIdDuplicate(String loginId);

    public Member findByLoginId(String loginId);
}
