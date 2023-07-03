package com.plee.library.dto.admin.request;

import com.plee.library.domain.member.Role;
import jakarta.validation.constraints.Size;
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

    private String newPassword;

    private Role role;
}
