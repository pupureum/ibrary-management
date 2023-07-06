package com.plee.library.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "member")
//@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : member") //TODO null checking 필요할지 고민
public @interface CurrentMember {
}