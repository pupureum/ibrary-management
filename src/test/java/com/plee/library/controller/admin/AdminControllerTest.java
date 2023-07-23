package com.plee.library.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plee.library.config.TestUserDetailsConfig;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.admin.request.DeleteBookRequest;
import com.plee.library.dto.admin.request.SearchBookRequest;
import com.plee.library.dto.admin.request.UpdateBookRequest;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.admin.response.BooksResponse;
import com.plee.library.dto.admin.response.LoanStatusResponse;
import com.plee.library.dto.admin.response.LoanDailyStatusResponse;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.book.response.CategoryResponse;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    @DisplayName("POST /admin/new-book 도서 추가 요청")
    class AddBookTest {

        @Test
        @DisplayName("도서 추가 성공")
        void addBook_Success() throws Exception {
            // given
            SaveBookRequest req = SaveBookRequest.builder()
                    .isbn("9788994492032")
                    .title("자바의 정석")
                    .author("남궁성")
                    .categoryId(1L)
                    .quantity(2)
                    .build();
            willDoNothing().given(bookService).saveBook(req);

            // when, then
            mockMvc.perform(post("/admin/new-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패: 수량이 0인 경우")
        void addBook_FailQuantity() throws Exception {
            // given
            SaveBookRequest req = SaveBookRequest.builder()
                    .isbn("9788994492032")
                    .title("자바의 정석")
                    .categoryId(1L)
                    .quantity(0)
                    .build();
            willDoNothing().given(bookService).saveBook(req);

            // when, then
            mockMvc.perform(post("/admin/new-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("1 ~ 9999 까지의 수량만 입력 가능합니다."));;
        }

        @Test
        @DisplayName("실패: 카테고리 선택 없는 경우")
        void addBook_FailCategory() throws Exception {
            // given
            SaveBookRequest req = SaveBookRequest.builder()
                    .isbn("9788994492032")
                    .title("자바의 정석")
                    .quantity(1)
                    .build();
            willDoNothing().given(bookService).saveBook(req);

            // when, then
            mockMvc.perform(post("/admin/new-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("카테고리를 선택해주세요."));;
        }

        @Test
        @DisplayName("실패: 이미 존재하는 도서 요청")
        void addBook_Fail() throws Exception {
            // given
            SaveBookRequest req = SaveBookRequest.builder()
                    .isbn("9788994492032")
                    .title("자바의 정석")
                    .author("남궁성")
                    .categoryId(1L)
                    .quantity(2)
                    .build();
            willThrow(new IllegalStateException(BookMessage.ALREADY_EXIST_BOOK.getMessage())).given(bookService).saveBook((any(SaveBookRequest.class)));

            // when, then
            mockMvc.perform(post("/admin/new-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(BookMessage.ALREADY_EXIST_BOOK.getMessage()));
        }
    }

    @Nested
    @WithUserDetails
    @DisplayName("GET /admin/books/category/{categoryId} 카테고리별 도서 목록 조회 요청")
    class CategoryBooksTest {
        private Page<BooksResponse> booksPage;

        @BeforeEach
        void setUp() {
            List<BooksResponse> books = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            booksPage = new PageImpl<>(books, pageable, 0);
        }

        @Test
        @DisplayName("카테고리 도서 목록 조회 성공")
        void categoryBooks_Success() throws Exception {
            // given
            Long categoryId = 1L;
            given(bookService.findBooksByCategory(anyLong(), any(Pageable.class))).willReturn(booksPage);
            given(bookService.findCategories()).willReturn(Collections.emptyList());

            // when, then
            mockMvc.perform(get("/admin/books/category/{categoryId}", categoryId)
                            .param("page", "1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/bookList"))
                    .andExpect(model().attribute("books", booksPage))
                    .andExpect(model().attribute("categories", Collections.emptyList()))
                    .andExpect(model().attribute("selectedCategory", categoryId));
            then(bookService).should(times(1)).findBooksByCategory(anyLong(), any(Pageable.class));
            then(bookService).should(times(1)).findCategories();
        }

        @Test
        @DisplayName("실패: 카테고리가 없는 경우")
        void searchBookByCategory_Fail() throws Exception {
            // given
            given(bookService.findBooksByCategory(anyLong(), any(Pageable.class))).willReturn(booksPage);
            given(bookService.findCategories()).willThrow(new NoSuchElementException(BookMessage.NOT_FOUND_CATEGORY.getMessage()));

            // when, then
            mockMvc.perform(get("/admin/books/category/{categoryId}", 1L)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/books"))
                    .andExpect(flash().attributeExists("errorMessage"))
                    .andExpect(flash().attribute("errorMessage", BookMessage.NOT_FOUND_CATEGORY.getMessage()));
        }
    }

    @Nested
    @WithUserDetails
    @DisplayName("GET /admin/books/search 도서 검색 요청")
    class SearchBooksTest {
        @Test
        @DisplayName("도서 검색 성공")
        void searchBooks_Success() throws Exception {
            // given
            SearchBookRequest req = SearchBookRequest.builder()
                    .keyword("java")
                    .categoryId(1L)
                    .build();
            Page<BooksResponse> res = new PageImpl<>(Collections.emptyList());
            given(bookService.searchBooks(any(SearchBookRequest.class), any(Pageable.class))).willReturn(res);
            given(bookService.findCategories()).willReturn(Collections.emptyList());

            // when, then
            mockMvc.perform(get("/admin/books/search")
                            .flashAttr("searchBookRequest", req)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/bookList"))
                    .andExpect(model().attribute("books", res))
                    .andExpect(model().attribute("categories", Collections.emptyList()))
                    .andExpect(model().attribute("selectedCategory", req.getCategoryId()));
            then(bookService).should(times(1)).searchBooks(any(SearchBookRequest.class), any(Pageable.class));
            then(bookService).should(times(1)).findCategories();
        }

        @Test
        @DisplayName("실패: 키워드가 공백인 경우")
        void searchBooks_Fail() throws Exception {
            // given
            SearchBookRequest req = SearchBookRequest.builder()
                    .keyword(" ")
                    .before(0)
                    .build();

            // when, then
            mockMvc.perform(get("/admin/books/search")
                            .flashAttr("searchBookRequest", req)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"))
                    .andExpect(redirectedUrl("/admin/books?page="+req.getBefore()))
                    .andExpect(flash().attribute("errorMessage", "검색어를 입력해주세요."));
        }
    }

    @Nested
    @WithUserDetails
    @DisplayName("PUT /admin/books/{bookId} 도서 재고 수량 수정 요청")
    class UpdateBookQuantityTest {
        @Test
        @DisplayName("도서 정보 수정 성공 (기본 사내 도서 목록 화면에서 요청)")
        void updateBookQuantity_Success() throws Exception {
            // given
            Long bookId = 1L;
            UpdateBookRequest req = UpdateBookRequest.builder()
                    .newQuantity(2)
                    .categoryId(null)
                    .keyword("")
                    .page(0)
                    .build();
            willDoNothing().given(bookService).updateBookQuantity(bookId, req);

            // when, then
            mockMvc.perform(put("/admin/books/{bookId}", bookId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .flashAttr("updateBookRequest", req))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/books" + "?page=" + req.getPage()))
                    .andExpect(flash().attribute("successMessage", BookMessage.SUCCESS_UPDATE_QUANTITY.getMessage()));
        }

        @Test
        @DisplayName("실패: 대출중인 수량보다 적은 수량으로 수정 요청 (카테고리 선택 화면에서 요청)")
        void updateBookQuantity_Fail() throws Exception {
            // given
            Long bookId = 1L;
            UpdateBookRequest req = UpdateBookRequest.builder()
                    .newQuantity(1)
                    .categoryId(2L)
                    .keyword("")
                    .page(2)
                    .build();
            willThrow(new IllegalArgumentException(BookMessage.CANNOT_UPDATE_QUANTITY.getMessage())).given(bookService).updateBookQuantity(eq(bookId), any(UpdateBookRequest.class));

            // when, then
            mockMvc.perform(put("/admin/books/{bookId}", bookId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .flashAttr("updateBookRequest", req))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/books/category/" + req.getCategoryId() + "?page=" + req.getPage()))
                    .andExpect(flash().attributeExists("errorMessage"))
                    .andExpect(flash().attribute("errorMessage", BookMessage.CANNOT_UPDATE_QUANTITY.getMessage()));
        }

        @Test
        @DisplayName("실패: 기존 수량과 같은 수량으로 수정 요청 (키워드 검색 페이지에서 들어온 요청)")
        void updateBookQuantity_FailSameQuantity() throws Exception {
            // given
            Long bookId = 1L;
            UpdateBookRequest req = UpdateBookRequest.builder()
                    .newQuantity(1)
                    .categoryId(1L)
                    .keyword("스프링 부트")
                    .page(0)
                    .build();
            willThrow(new IllegalArgumentException(BookMessage.CANNOT_UPDATE_SAME_QUANTITY.getMessage())).given(bookService).updateBookQuantity(eq(bookId), any(UpdateBookRequest.class));

            // when, then
            mockMvc.perform(put("/admin/books/{bookId}", bookId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .flashAttr("updateBookRequest", req))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            UriComponentsBuilder.fromPath("/admin/books/search")
                                    .queryParam("keyword", req.getKeyword())
                                    .queryParam("page", req.getPage())
                                    .queryParam("categoryId", req.getCategoryId())
                                    .toUriString()))
                    .andExpect(flash().attributeExists("errorMessage"))
                    .andExpect(flash().attribute("errorMessage", BookMessage.CANNOT_UPDATE_SAME_QUANTITY.getMessage()));
        }
    }

    @Nested
    @WithUserDetails
    @DisplayName("DELETE /admin/books/{bookId} 도서 삭제 요청")
    class DeleteBookTest {
        @Test
        @DisplayName("기본 사내 도서 목록 화면에서 들어온 요청")
        void deleteBook() throws Exception {
            // given
            Long bookId = 1L;
            DeleteBookRequest req = DeleteBookRequest.builder()
                    .categoryId(null)
                    .keyword("")
                    .page(2)
                    .build();
            willDoNothing().given(bookService).deleteBook(bookId);

            // when, then
            mockMvc.perform(delete("/admin/books/{bookId}", bookId)
                            .flashAttr("deleteBookRequest", req))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/books" + "?page=" + req.getPage()))
                    .andExpect(flash().attribute("successMessage", BookMessage.SUCCESS_DELETE_BOOK.getMessage()));
        }

        @Test
        @DisplayName("키워드 검색 없이 카테고리 화면에서 들어온 요청")
        void deleteBook_InCategory() throws Exception {
            // given
            Long bookId = 1L;
            DeleteBookRequest req = DeleteBookRequest.builder()
                    .categoryId(2L)
                    .keyword("")
                    .page(0)
                    .build();
            willDoNothing().given(bookService).deleteBook(bookId);

            // when, then
            mockMvc.perform(delete("/admin/books/{bookId}", bookId)
                            .flashAttr("deleteBookRequest", req))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/books/category/" + req.getCategoryId() + "?page=" + req.getPage()))
                    .andExpect(flash().attribute("successMessage", BookMessage.SUCCESS_DELETE_BOOK.getMessage()));
        }

        @Test
        @DisplayName("키워드 검색 화면에서 들어온 요청")
        void deleteBook_InSearch() throws Exception {
            // given
            Long bookId = 1L;
            DeleteBookRequest req = DeleteBookRequest.builder()
                    .categoryId(3L)
                    .keyword("자바")
                    .page(0)
                    .build();
            willDoNothing().given(bookService).deleteBook(bookId);

            // when, then
            mockMvc.perform(delete("/admin/books/{bookId}", bookId)
                            .flashAttr("deleteBookRequest", req))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            UriComponentsBuilder.fromPath("/admin/books/search")
                                    .queryParam("keyword", req.getKeyword())
                                    .queryParam("page", req.getPage())
                                    .queryParam("categoryId", req.getCategoryId())
                                    .toUriString()))
                    .andExpect(flash().attribute("successMessage", BookMessage.SUCCESS_DELETE_BOOK.getMessage()));
        }
    }

    @Test
    @WithUserDetails
    @DisplayName("GET /admin/loan 대출 현황 조회 요청")
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
        List<LoanStatusResponse> hisRes = Arrays.asList(
                LoanStatusResponse.builder()
                        .id(1L)
                        .member(Member.builder().id(1L).loginId("test@gmail.com").build())
                        .bookInfo(bookInfo)
                        .isRenew(true)
                        .loanedAt(LocalDate.now())
                        .build()
        );
        Page<LoanStatusResponse> pageRes = new PageImpl<>(hisRes, pageable, hisRes.size());

        // 대출 수 데이터 생성
        LoanDailyStatusResponse dataRes = new LoanDailyStatusResponse(new HashMap<>());

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
    @DisplayName("PUT /admin/members/{memberId} 회원 정보 수정")
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
    @DisplayName("DELETE /admin/members/{memberId} 회원 삭제 요청")
    class DeleteMemberTest {
        @Test
        @DisplayName("삭제 성공")
        void deleteMember_Success() throws Exception {
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
        @DisplayName("실패: 요청한 회원이 없는 경우")
        void deleteMember_Fail() throws Exception {
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