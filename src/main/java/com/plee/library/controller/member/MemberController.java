package com.plee.library.controller.member;

import com.plee.library.dto.member.request.LoginMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
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
    public String editInfoForm(Model model, Principal principal) {
        model.addAttribute("member", memberService.findMember(principal.getName()));
        model.addAttribute("selectedMenu", "member-edit-info");
        return "member/editInfo";
    }

    @PutMapping("/edit/{memberId}")
    public ResponseEntity<String> editInfo(@PathVariable Long memberId, @Validated UpdateMemberRequest request) {
        log.info("editInfo request");
        System.out.println("memberId = " + memberId);
        System.out.println("memberName = " + request.getName());
        System.out.println("request = " + request.getNewPassword());
        memberService.updateMemberInfo(memberId, request);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/edit/current-password")
    public ResponseEntity<Boolean> checkCurrentPassword(@RequestParam("currentPassword") String currentPassword, Principal principal) {
        boolean isPasswordMatched = memberService.checkCurrentPassword(currentPassword, principal.getName());
        return ResponseEntity.ok(isPasswordMatched); //TODO Pincipal로 받을것인지 고민 필요!!!
    }
}
