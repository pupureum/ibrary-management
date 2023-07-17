package com.plee.library.dto.member.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SignUpMemberRequest {

    @Size(min = 1, max = 25, message = "이름은 1~25자 이내여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영어로만 입력해야 합니다.")
    private String name;

    @Email
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@gmail\\.com$", message = "이메일은 @gmail.com 형식이어야 합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 6, max = 20, message = "비밀번호는 6~20자 이내여야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    @Size(min = 6, max = 20, message = "비밀번호는 6~20자 이내여야 합니다.")
    private String confirmPassword;
}