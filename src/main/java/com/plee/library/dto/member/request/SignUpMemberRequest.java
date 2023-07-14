package com.plee.library.dto.member.request;

import com.plee.library.domain.member.Member;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SignUpMemberRequest {

    @NotEmpty(message = "이름을 입력해주세요.")
    @Size(min = 1, max = 25, message = "이름은 1~25자 이내여야 합니다.")
    private String name;

    @NotBlank(message = "공백없는 이메일을 입력해주세요.")
    @Email
    @Size(min = 1, max = 40, message = "1글자 이상 40글자 이내로 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@gmail\\.com$", message = "이메일은 @gmail.com 형식이어야 합니다.")
    private String loginId;

    @NotBlank(message = "공백없는 비밀번호를 입력해주세요.")
//    @Size(min = 6, max = 20, message = "비밀번호는 6~20자 이내여야 합니다.")
    private String password;

    @NotBlank(message = "공백없는 비밀번호를 입력해주세요.")
//    @Size(min = 6, max = 20, message = "비밀번호는 6~20자 이내여야 합니다.")
    private String confirmPassword;

    @Builder
    public SignUpMemberRequest(String name, String loginId, String password, String confirmPassword) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public Member toEntity() {
        return Member.builder()
                .name(this.name)
                .loginId(this.loginId)
                .password(this.password)
                .build();
    }
}