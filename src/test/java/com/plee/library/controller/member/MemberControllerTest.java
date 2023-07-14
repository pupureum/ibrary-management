package com.plee.library.controller.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plee.library.config.MemberAdapter;
import com.plee.library.config.TestUserDetailsConfig;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.member.request.SignUpMemberRequest;
import com.plee.library.dto.member.response.MemberInfoResponse;
import com.plee.library.util.message.MemberMessage;
import com.plee.library.service.member.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestUserDetailsConfig.class)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(MemberController.class)
@DisplayName("MemberController 테스트")
class MemberControllerTest {

    @MockBean
    private MemberService memberService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .addFilter(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .build();
    }

    @Nested
    @WithAnonymousUser
    @DisplayName("GET 로그인 페이지 반환 (익명의 사용자)")
    class GetLoginFormTest {
        @Test
        @DisplayName("에러 없는 경우")
        void loginForm() throws Exception {
            // when, then
            mockMvc.perform(get("/member/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/login"))
                    .andExpect(model().attributeDoesNotExist("error"));
        }

        @Test
        @DisplayName("에러 있는 경우")
        void loginFormWithError() throws Exception {
            // given
            String error = "error";

            // when, then
            mockMvc.perform(get("/member/login")
                            .param("error", error))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/login"))
                    .andExpect(model().attribute("error", error));
        }
    }

    @Test
    @WithMockUser
    @DisplayName("GET 로그아웃 처리 후 홈 페이지로 리다이렉트")
    void logout() throws Exception {
        // when, then
        mockMvc.perform(get("/member/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET 회원가입 페이지 반환 (익명의 사용자)")
    void signupForm() throws Exception {
        // when, then
        mockMvc.perform(get("/member/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup"));
    }

    @Nested
    @WithAnonymousUser
    @DisplayName("POST 회원가입 요청")
    class SignupTest {
        @Test
        @DisplayName("회원가입 성공")
        void signupSuccess() throws Exception {
            // given
            // 회원가입 요청 dto 생성
            SignUpMemberRequest req = SignUpMemberRequest.builder()
                    .name("이푸름")
                    .loginId("plee@gmail.com")
                    .password("password123")
                    .confirmPassword("password123")
                    .build();

            // when, then
            mockMvc.perform(post("/member/signup")
                            .flashAttr("signUpMemberRequest", req))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/member/login"));
        }

        @Test
        @DisplayName("실패: 유효하지 않은 dto 요청")
        void signupFail_invalidLoginId() throws Exception {
            // given
            // 회원가입 요청 dto 생성 (아이디 형식 오류)
            SignUpMemberRequest req = SignUpMemberRequest.builder()
                    .name("이푸름")
                    .loginId("test@test.com")
                    .password("password123")
                    .confirmPassword("password123")
                    .build();

            // when, then
            // 검증 실패로 인해 회원가입 반환
            MvcResult result = mockMvc.perform(post("/member/signup")
                            .flashAttr("signUpMemberRequest", req))
                    .andExpect(view().name("member/signup"))
                    .andExpect(model().attributeExists("signUpMemberRequest"))
                    .andExpect(model().hasErrors())
                    .andExpect(view().name("member/signup"))
                    .andReturn();

            // Field error의 default message 검증
            BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.signUpMemberRequest");
            FieldError fieldError = bindingResult.getFieldError("loginId");
            assertEquals("이메일은 @gmail.com 형식이어야 합니다.", fieldError.getDefaultMessage());
        }
    }


    @Test
    @WithUserDetails
    @DisplayName("GET 회원 정보 수정 페이지 반환")
    void editInfoForm() throws Exception {
        // given
        Member member = ((MemberAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getMember();
        MemberInfoResponse response = MemberInfoResponse.from(member);
        given(memberService.findMember(member.getId())).willReturn(response);

        // when, then
        mockMvc.perform(get("/member/edit")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/editInfo"))
                // 모델에 담긴 member 객체의 타입이 MemberInfoResponse인지 검증
                .andExpect(model().attribute("member", response))
                .andExpect(model().attribute("selectedMenu", "member-edit-info"));
    }

    @Test
    @WithUserDetails
    @DisplayName("GET 현재 비밀번호 확인 요청 처리")
    void checkCurrentPassword() throws Exception {
        // given
        given(memberService.checkCurrentPassword(anyString(), anyLong())).willReturn(true);

        // when, then
        mockMvc.perform(get("/member/edit/current-password")
                        .param("currentPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Nested
    @WithUserDetails
    @DisplayName("GET 회원 정보 변경 요청 처리")
    class EditInfoTest {
        @Test
        @DisplayName("정보 변경 성공")
        void editInfo_success() throws Exception {
            // given
            UpdateMemberRequest req = new UpdateMemberRequest();

            // when, then
            mockMvc.perform(put("/member/edit/{memberId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(req)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("success"));
        }

        @Test
        @DisplayName("실패: 현재 접속된 회원과, 수정하려는 회원이 다른 경우")
        void editInfo_fail() throws Exception {
            // given
            UpdateMemberRequest req = new UpdateMemberRequest();

            // when, then
            mockMvc.perform(put("/member/edit/{memberId}", 3L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(MemberMessage.INVALID_ACCESS.getMessage()));
        }
    }

    private String asJsonString(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}