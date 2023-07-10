package com.plee.library.controller.member;

import com.plee.library.annotation.CurrentMember;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.admin.response.AllMemberInfoResponse;
import com.plee.library.dto.member.response.MemberInfoResponse;
import com.plee.library.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 로그인 페이지를 반환합니다.
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        log.info("GET login Form");
        model.addAttribute("error", error);
        return "member/login";
    }

    // 로그아웃 요청을 처리하고 홈 페이지로 리다이렉트합니다.
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("GET logout request");
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/";
    }

    // 회원가입 페이지를 반환합니다.
    @GetMapping("/signup")
    public String signupForm(Model model) {
        log.info("GET signupForm request");
        model.addAttribute("signUpMemberRequest", new SignUpMemberRequest());
        return "member/signup";
    }

    // 회원가입 요청을 처리하고 로그인 페이지로 리다이렉트합니다.
    @PostMapping("/signup")
    public String signup(@Validated @ModelAttribute SignUpMemberRequest request, BindingResult bindingResult) {
        log.info("POST signup request");

        memberService.validateSignupRequest(request, bindingResult);
        // 검증에 실패하면 다시 회원가입 폼으로 리다이렉트
        if (bindingResult.hasErrors()) {
            log.info("binding errors = {} ", bindingResult);
            return "member/signup";
        }

        memberService.saveMember(request);
        return "redirect:/member/login";
    }

    // 회원 정보 페이지를 반환합니다.
    @GetMapping("/edit")
    public String editInfoForm(@CurrentMember Member member, Model model) {
        log.info("GET editInfoForm request memberId = {}", member.getLoginId());
        MemberInfoResponse memberInfoResponse = memberService.findMember(member.getId());

        model.addAttribute("member", memberInfoResponse);
        model.addAttribute("selectedMenu", "member-edit-info");
        return "member/editInfo";
    }

    // 현재 비밀번호와 입력받은 비밀번호의 일치 여부 확인 요청을 처리합니다.
    @GetMapping("/edit/current-password")
    public ResponseEntity<Boolean> checkCurrentPassword(@RequestParam("currentPassword") String currentPassword, @CurrentMember Member member) {
        log.info("GET checkCurrentPassword request member = {}", member.getLoginId());
        boolean isPasswordMatched = memberService.checkCurrentPassword(currentPassword, member);
        return ResponseEntity.ok(isPasswordMatched);
    }

    // 회원 정보 변경 요청을 처리합니다.
    @PutMapping("/edit/{memberId}")
    public ResponseEntity<String> editInfo(@PathVariable Long memberId, @Validated UpdateMemberRequest request, @CurrentMember Member member) {
        log.info("PUT editInfo request member = {}", member.getLoginId());

        // 현재 로그인된 회원과 수정하려는 회원이 같은지 확인
        if (!member.getId().equals(memberId)) {
            return ResponseEntity.badRequest().body("잘못된 접근입니다.");
        }

        memberService.changeMemberInfo(memberId, request);
        return ResponseEntity.ok("success");
    }

}
