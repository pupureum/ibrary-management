package com.plee.library.controller.member;

import com.plee.library.domain.member.Member;
import com.plee.library.dto.member.request.LoginMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    //    @GetMapping("/login")
//    public String loginForm(Model model) {
//        log.info("login Form resolve");
//        model.addAttribute("loginMemberRequest", new LoginMemberRequest());
//        return "member/login";
//    }
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,Model model) {
        log.info("login Form resolve");
        model.addAttribute("loginMemberRequest", new LoginMemberRequest());
        model.addAttribute("error", error);
        return "member/login";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute LoginMemberRequest request, BindingResult bindingResult, Model model) {
        System.out.println("-------------------");
        log.info("login request={}", request);
        if (bindingResult.hasErrors()) {
            log.info("binding errors={} ", bindingResult);
            return "member/login";
        }
        Member loginMember = memberService.login(request);
        if (loginMember == null) {
            System.out.println("3");
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "member/login";
        }
        log.info("login success");
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Model model) {
        log.info("logout");
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signUpMemberRequest", new SignUpMemberRequest());
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(@Validated @ModelAttribute SignUpMemberRequest request, BindingResult bindingResult, Model model) {
        log.info("signup request");
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.reject("passwordNotMatch", "비밀번호가 일치하지 않습니다.");
        }
        if (memberService.checkLoginIdDuplicate(request.getLoginId())) {
            bindingResult.reject("duplicateLoginId", "이미 존재하는 아이디입니다.");
        }
        if (bindingResult.hasErrors()) {
            log.info("binding errors={} ", bindingResult);
            return "member/signup";
        }
        memberService.saveMember(request);
        return "redirect:/member/login";
    }

    @GetMapping("/edit")
    public String editInformationForm(Model model) {
        model.addAttribute("isAdmin", true);
//        model.addAttribute("isAdmin", false);
//        model.addAttribute("totalCount", 100);
        model.addAttribute("selectedMenu", "member-edit-info");
        return "member/editInfo";
    }

}
