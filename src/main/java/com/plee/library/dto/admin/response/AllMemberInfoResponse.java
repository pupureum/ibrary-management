package com.plee.library.dto.member.response;

import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<MemberInfoResponse> from(Page<Member> members) {
        return members.stream()
                .map(member -> MemberInfoResponse.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .loginId(member.getLoginId())
                        .password(member.getPassword())
                        .role(member.getRole())
                        .createdAt(member.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
    }
}
