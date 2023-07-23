package com.plee.library.dto.admin.request;

import com.plee.library.domain.member.Role;
import jakarta.validation.constraints.Pattern;
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
    @Size(min = 1, max = 25, message = "이름은 1~25자 이내여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영어로만 입력해야 합니다.")
    private String name;

    private String password;

    private Role role;
}
