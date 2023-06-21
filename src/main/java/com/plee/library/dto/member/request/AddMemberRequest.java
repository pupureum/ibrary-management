package com.plee.library.dto.member.request;

import com.plee.library.domain.member.Member;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    @NotBlank
    @Size(min = 1, max = 25)
    private String name;
    @NotBlank
    @Email
    @Max(40)
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@gmail\\.com$", message = "로그인 아이디는 @gmail.com 형식이어야 합니다.")
    private String loginId;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    public Member toEntity() {
        return Member.builder()
                .name(this.name)
                .loginId(this.loginId)
                .password(this.password)
                .build();
    }
}