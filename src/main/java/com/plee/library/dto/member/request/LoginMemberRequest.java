package com.plee.library.dto.member.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginMemberRequest {

    @NotBlank
    @Email
    @Size(min = 1, max = 40, message = "1글자 이상 40글자 이내로 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@gmail\\.com$", message = "이메일은 @gmail.com 형식이어야 합니다.")
    private String loginId;

    @NotBlank
//    @Size(min = 6, max = 20 , message = "비밀번호는 6~20자 이내여야 합니다.")
    private String password;
}
