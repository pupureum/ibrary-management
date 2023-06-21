package com.plee.library.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    User("ROLE_USER"),
    Admin("ROLE_ADMIN");

    private final String type;

    public String getType() {
    	return type;
    }
}
