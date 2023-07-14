package com.plee.library.util.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberMessage implements Message {
    SUCCESS_DELETE_MEMBER("회원이 삭제되었습니다."),
    NOT_FOUND_MEMBER("회원 정보를 찾을 수 없습니다."),
    DUPLICATE_LOGIN_ID("이미 존재하는 아이디입니다."),
    NOT_CHANGED_PASSWORD("기존 비밀번호와 동일합니다."),
    INVALID_ACCESS("잘못된 접근입니다."),
    NOT_MATCHED_PASSWORD("비밀번호가 일치하지 않습니다.");

    private final String message;
}
