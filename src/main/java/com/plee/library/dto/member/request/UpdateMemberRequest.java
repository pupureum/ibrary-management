package com.plee.library.dto.member.request;

import com.plee.library.domain.member.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRequest {
    private String name;
    private Role role;
    private String password;
}
