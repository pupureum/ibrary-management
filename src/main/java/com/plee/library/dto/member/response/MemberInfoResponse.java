package com.plee.library.dto.member.response;

import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MemberInfoResponse {
    private final Long id;
    private final String name;
    private final String loginId;
    private final String password;
    private final Role role;

    private final LocalDate createdAt;

    @Builder
    public MemberInfoResponse(Long id, String name, String loginId, String password, Role role, LocalDate createdAt) {
        this.id = id;
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static MemberInfoResponse from(Member member) {
        return MemberInfoResponse.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .loginId(member.getLoginId())
                        .password(member.getPassword())
                        .role(member.getRole())
                        .createdAt(member.getCreatedAt().toLocalDate())
                        .build();
    }
}
