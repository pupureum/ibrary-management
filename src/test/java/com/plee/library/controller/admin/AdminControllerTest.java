package com.plee.library.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plee.library.config.TestUserDetailsConfig;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.admin.request.UpdateBookRequest;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.admin.response.AllLoanHistoryResponse;
import com.plee.library.dto.admin.response.LoanStatusResponse;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.util.message.BookMessage;
import com.plee.library.util.message.MemberMessage;
import com.plee.library.service.book.BookService;
import com.plee.library.service.member.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestUserDetailsConfig.class)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(AdminController.class)
@DisplayName("AdminController 테스트")
class AdminControllerTest {

    @MockBean
    private BookService bookService;

    @MockBean
    private MemberService memberService;

    @Autowired
    ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .addFilter(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .build();
    }

    @Nested
    @WithUserDetails
    @DisplayName("POST 도서 추가 요청")
    class AddBookTest {
        private SaveBookRequest req;

        @BeforeEach
        void setUp() {
            req = SaveBookRequest.builder()
                    .isbn("9788994492032")
                    .title("자바의 정석")
                    .author("남궁성")
                    .quantity(2)
                    .build();
        }

        @Test
        @DisplayName("도서 추가 성공")
        void addBook() throws Exception {
            // given
            willDoNothing().given(bookService).saveBook(req);

            // when, then
            mockMvc.perform(post("/admin/new-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패: 이미 존재하는 도서 요청")
        void addBook_fail() throws Exception {
            // given
            willThrow(new IllegalStateException(BookMessage.ALREADY_EXIST_BOOK.getMessage())).given(bookService).saveBook(any(SaveBookRequest.class));

            // when, then
            mockMvc.perform(post("/admin/new-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(BookMessage.ALREADY_EXIST_BOOK.getMessage()));
        }
    }

    @Nested
    @DisplayName("도서 정보 수정 요청")
    class UpdateBookQuantityTest {
        @Test
        @DisplayName("도서 정보 수정 성공")
        void updateBookQuantity() throws Exception {
            // given
            Long bookId = 1L;
            UpdateBookRequest req = new UpdateBookRequest(3);
            willDoNothing().given(bookService).updateBookQuantity(bookId, req);

            // when, then
            mockMvc.perform(put("/admin/books/{bookId}", bookId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("newQuantity", String.valueOf(req.getNewQuantity())))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/books"))
                    .andExpect(flash().attribute("successMessage", BookMessage.SUCCESS_UPDATE_QUANTITY.getMessage()));
        }

        @Test
        @DisplayName("실패: 대출중인 수량보다 적은 수량으로 수정 요청")
        void updateBookQuantity_fail() throws Exception {
            // given
            Long bookId = 1L;
            UpdateBookRequest req = new UpdateBookRequest(2);
            willThrow(new IllegalArgumentException(BookMessage.CANNOT_UPDATE_QUANTITY.getMessage())).given(bookService).updateBookQuantity(eq(bookId), any(UpdateBookRequest.class));

            // when, then
            mockMvc.perform(put("/admin/books/{bookId}", bookId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("newQuantity", String.valueOf(req.getNewQuantity())))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/books"))
                    .andExpect(flash().attributeExists("errorMessage"))
                    .andExpect(flash().attribute("errorMessage", BookMessage.CANNOT_UPDATE_QUANTITY.getMessage()));
        }

        @Test
        @DisplayName("실패: 기존 수량과 같은 수량으로 수정 요청")
        void updateBookQuantity_fail_sameQuantity() throws Exception {
            // given
            Long bookId = 1L;
            UpdateBookRequest req = new UpdateBookRequest(2);
            willThrow(new IllegalArgumentException(BookMessage.CANNOT_UPDATE_SAME_QUANTITY.getMessage())).given(bookService).updateBookQuantity(eq(bookId), any(UpdateBookRequest.class));

            // when, then
            mockMvc.perform(put("/admin/books/{bookId}", bookId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("newQuantity", String.valueOf(req.getNewQuantity())))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/books"))
                    .andExpect(flash().attributeExists("errorMessage"))
                    .andExpect(flash().attribute("errorMessage", BookMessage.CANNOT_UPDATE_SAME_QUANTITY.getMessage()));
        }
    }

    @Test
    @DisplayName("도서 삭제 요청")
    void deleteBook() throws Exception {
        // given
        Long bookId = 1L;
        willDoNothing().given(bookService).deleteBook(bookId);

        // when, then
        mockMvc.perform(delete("/admin/books/{bookId}", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"))
                .andExpect(flash().attribute("successMessage", BookMessage.SUCCESS_DELETE_BOOK.getMessage()));
    }

    @Test
    @DisplayName("대출 현황 조회 요청")
    void loanStatus() throws Exception {
        // given
        // 대출 기록 생성
        BookInfo bookInfo = BookInfo
                .builder()
                .isbn("9788998274792")
                .title("이것이 자바다")
                .author("신용권")
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AllLoanHistoryResponse> hisRes = Arrays.asList(
                AllLoanHistoryResponse.builder()
                        .id(1L)
                        .member(Member.builder().id(1L).loginId("test@gmail.com").build())
                        .bookInfo(bookInfo)
                        .isRenew(true)
                        .loanedAt(LocalDate.now())
                        .build()
        );
        Page<AllLoanHistoryResponse> pageRes = new PageImpl<>(hisRes, pageable, hisRes.size());

        // 대출 수 데이터 생성
        LoanStatusResponse dataRes = new LoanStatusResponse(new HashMap<>());

        given(bookService.findAllLoanHistory(any(Pageable.class))).willReturn(pageRes);
        given(bookService.calculateDailyLoanCounts()).willReturn(dataRes);

        // when, then
        mockMvc.perform(get("/admin/loan"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/loanStatus"))
                .andExpect(model().attributeExists("loanHistory"))
                .andExpect(model().attributeExists("data"))
                .andExpect(model().attributeExists("selectedMenu"));
    }

    @Test
    @WithUserDetails
    @DisplayName("회원 정보 수정")
    void updateMemberInfo() throws Exception {
        // given
        UpdateMemberRequest req = new UpdateMemberRequest();
        willDoNothing().given(memberService).updateMemberByAdmin(anyLong(), any(UpdateMemberRequest.class));

        // when, then
        mockMvc.perform(put("/admin/members/{memberId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
        then(memberService).should(times(1)).updateMemberByAdmin(anyLong(), any(UpdateMemberRequest.class));
    }

    @Nested
    @DisplayName("회원 삭제 요청")
    class DeleteMemberTest {
        @Test
        @DisplayName("삭제 성공")
        void deleteMember() throws Exception {
            // given
            willDoNothing().given(memberService).deleteMember(anyLong());

            // when, then
            mockMvc.perform(delete("/admin/members/{memberId}", 1L))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/members"))
                    .andExpect(flash().attribute("successMessage", MemberMessage.SUCCESS_DELETE_MEMBER.getMessage()));
            then(memberService).should(times(1)).deleteMember(anyLong());
        }

        @Test
        @DisplayName("실패: 요청한 회원이 존재하지 않을 경우")
        void deleteMember_fail() throws Exception {
            // given
            willThrow(new NoSuchElementException(MemberMessage.NOT_FOUND_MEMBER.getMessage())).given(memberService).deleteMember(anyLong());

            // when, then
            mockMvc.perform(delete("/admin/members/{memberId}", 1L))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/members"))
                    .andExpect(flash().attributeExists("errorMessage"))
                    .andExpect(flash().attribute("errorMessage", MemberMessage.NOT_FOUND_MEMBER.getMessage()));
            then(memberService).should(times(1)).deleteMember(anyLong());
        }

    }
}