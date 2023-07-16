package com.plee.library.controller.admin;

import com.plee.library.dto.admin.request.DeleteBookRequest;
import com.plee.library.dto.admin.request.SearchBookRequest;
import com.plee.library.dto.admin.request.UpdateBookRequest;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.admin.response.LoanHistoryResponse;
import com.plee.library.dto.admin.response.LoanStatusResponse;
import com.plee.library.dto.admin.response.RequestStatusResponse;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.admin.response.BooksResponse;
import com.plee.library.dto.admin.response.MemberInfoResponse;
import com.plee.library.dto.book.response.CategoryResponse;
import com.plee.library.util.message.BookMessage;
import com.plee.library.util.message.MemberMessage;
import com.plee.library.service.book.BookService;
import com.plee.library.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {
    private final BookService bookService;

    private final MemberService memberService;

    // 도서 추가 뷰를 카테고리 정보와 함께 반환합니다.
    @GetMapping("/new-book")
    public String addBookForm(Model model) {
        log.info("ADMIN GET addBookForm request");
        List<CategoryResponse> categories = bookService.findCategories();

        model.addAttribute("categories", categories);
        model.addAttribute("selectedMenu", "admin-new-book");
        return "admin/addBook";
    }

    // 도서 추가 요청을 처리합니다.
    @PostMapping("/new-book")
    public ResponseEntity<String> addBook(@Valid @RequestBody SaveBookRequest request, BindingResult bindingResult) {
        log.info("ADMIN POST addBook request, book = {}", request.getIsbn());
        // 유효성 검증 실패 시 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            log.warn("ADMIN POST addBook validation error");
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        try {
            bookService.saveBook(request);
        } catch (Exception e) {
            // 이미 존재하는 도서일 경우, 에러 메시지 반환
            log.warn("ADMIN POST addBook request failed", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 전체 도서 목록 페이지를 반환합니다.
    @GetMapping("/books")
    public String allBooks(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, Model model) {
        log.info("ADMIN GET allBooks request");
        Page<BooksResponse> response = bookService.findBooks(pageable);
        List<CategoryResponse> categories = bookService.findCategories();

        model.addAttribute("books", response);
        model.addAttribute("categories", categories);

        model.addAttribute("selectedMenu", "admin-book-list");
        return "admin/bookList";
    }

    // 카테고리별 도서 목록 페이지를 반환합니다.
    @GetMapping("/books/category/{categoryId}")
    public String categoryBooks(@PathVariable("categoryId") Long categoryId, @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable,
                                RedirectAttributes redirectAttributes, Model model) {
        log.info("ADMIN GET categoryBooks request");
        try {
            Page<BooksResponse> response = bookService.findBooksByCategory(categoryId, pageable);
            List<CategoryResponse> categories = bookService.findCategories();

            model.addAttribute("books", response);
            model.addAttribute("categories", categories);
        } catch (NoSuchElementException e) {
            // 카테고리 정보가 없는 경우
            log.warn("ADMIN GET categoryBooks request failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/books";
        }

        model.addAttribute("selectedCategory", categoryId);
        return "admin/bookList";
    }

    // 키워드 및 카테고리 내 도서 검색 결과를 반환합니다.
    @GetMapping("/books/search")
    public String searchBook(@Valid @ModelAttribute("searchBookRequest") SearchBookRequest request, BindingResult bindingResult,
                             @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, RedirectAttributes redirectAttributes, Model model) {
        log.info("ADMIN GET searchBook request, keyword = {} category = {}", request.getKeyword(), request.getCategoryId());
        // 유효성 검증 실패 시, 에러 메시지를 반환합니다.
        if (bindingResult.hasErrors()) {
            log.warn("ADMIN GET searchBook validation error");
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getFieldError().getDefaultMessage());
            // 유효성 검증 실패 시 검색 시도한 페이지로 리다이렉트하기 위해 page 정보 전달
            redirectAttributes.addAttribute("page", request.getBefore());
            return "redirect:/admin/books";
        }

        try {
            Page<BooksResponse> response = bookService.searchBooks(request, pageable);
            List<CategoryResponse> categories = bookService.findCategories();

            model.addAttribute("books", response);
            model.addAttribute("categories", categories);
        } catch (NoSuchElementException e) {
            // 카테고리 정보가 없는 경우
            log.warn("ADMIN GET searchBook request failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/books";
        }

        model.addAttribute("selectedCategory", request.getCategoryId());
        model.addAttribute("selectedMenu", "admin-book-list");
        return "admin/bookList";
    }

    // 신규 도서 추가 요청을 처리합니다.
    @PutMapping("/books/{bookId}")
    public String updateBookQuantity(@PathVariable Long bookId, @Valid @ModelAttribute("updateBookRequest") UpdateBookRequest request,
                                     BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("ADMIN PUT updateBookQuantity request, bookId = {}, quantity = {}", bookId, request.getNewQuantity());

        // 유효성 검사 실패시, 에러 메시지를 리다이렉트로 전달
        if (bindingResult.hasErrors()) {
            log.warn("ADMIN PUT updateBookQuantity validation error");
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/books";
        }

        try {
            bookService.updateBookQuantity(bookId, request);
            redirectAttributes.addFlashAttribute("successMessage", BookMessage.SUCCESS_UPDATE_QUANTITY.getMessage());
        } catch (Exception e) {
            log.warn("ADMIN PUT updateBookQuantity failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return redirectBasedOnRequest(request.getCategoryId(), request.getKeyword(), request.getPage());
    }

    // 도서 삭제 요청을 처리합니다.
    @DeleteMapping("/books/{bookId}")
    public String deleteBook(@PathVariable Long bookId, @ModelAttribute("deleteBookRequest") DeleteBookRequest request,
                             RedirectAttributes redirectAttributes) {
        try {
            log.info("ADMIN DELETE deleteBook request");
            bookService.deleteBook(bookId);
            redirectAttributes.addFlashAttribute("successMessage", BookMessage.SUCCESS_DELETE_BOOK.getMessage());
        } catch (NoSuchElementException e) {
            log.warn("ADMIN DELETE deleteBook failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return redirectBasedOnRequest(request.getCategoryId(), request.getKeyword(), request.getPage());
    }

    // 요청한 뷰에 따라 리다이렉트할 url을 반환합니다.
    private String redirectBasedOnRequest(Long categoryId, String keyword, int page) {
        // 카테고리 뷰에서 요청이 들어온 경우, 해당 페이지로 리다이렉트
        if (categoryId != null && keyword.isEmpty()) {

            return "redirect:/admin/books/category/" + categoryId + "?page=" + page;
        }
        // 검색 뷰에서 요청이 들어온 경우, 해당 페이지로 리다이렉트
        if (!keyword.isEmpty()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/books/search")
                    .queryParam("keyword", keyword)
                    .queryParam("page", page)
                    .queryParam("categoryId", categoryId);

            String redirectUrl = builder.toUriString();
            return "redirect:" + redirectUrl;
        }
        // 전체 도서 목록뷰에서 요청이 들어온 경우, 해당 페이지로 리다이렉트
        return "redirect:/admin/books" + "?page=" + page;

    }

    // 대출 현황 페이지를 반환합니다.
    @GetMapping("/loan")
    public String loanStatus(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, Model model) {
        log.info("ADMIN GET LoanStatus request");
        // 모든 대출 내역 조회
        Page<LoanHistoryResponse> loanHistory = bookService.findAllLoanHistory(pageable);

        // 최근 5일간 대출 빈도수 차트 데이터
        LoanStatusResponse data = bookService.calculateDailyLoanCounts();

        model.addAttribute("loanHistory", loanHistory);
        model.addAttribute("data", data);
        model.addAttribute("selectedMenu", "admin-loan-status");
        return "admin/loanStatus";
    }

    // 신규 도서 요청 페이지를 반환합니다.
    @GetMapping("/request")
    public String requestHistory(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, Model model) {
        log.info("ADMIN GET requestHistory request");
        Page<RequestStatusResponse> response = bookService.findAllNewBookReqHistory(pageable);

        model.addAttribute("requestHistory", response);
        model.addAttribute("selectedMenu", "admin-request-status");
        return "admin/requestStatus";
    }

    // 모든 회원 목록 페이지를 반환합니다.
    @GetMapping("/members")
    public String manageMember(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, Model model) {
        log.info("ADMIN GET manageMember request");
        Page<MemberInfoResponse> response = memberService.findAllMembers(pageable);

        model.addAttribute("members", response);
        model.addAttribute("selectedMenu", "admin-member-management");
        return "admin/memberManagement";
    }

    // 회원 정보 수정 요청을 처리합니다.
    @PutMapping("/members/{memberId}")
    public ResponseEntity<String> updateMemberInfo(@PathVariable Long memberId, @RequestBody UpdateMemberRequest request) {
        log.info("ADMIN PUT updateMemberInfo request, memberId = {}", memberId);
        try {
            memberService.updateMemberByAdmin(memberId, request);
        } catch (Exception e) {
            log.warn("ADMIN PUT updateMemberInfo failed", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok("Success");
    }

    // 회원 삭제 요청을 처리합니다.
    @DeleteMapping("/members/{memberId}")
    public String deleteMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        log.info("ADMIN DELETE deleteMember request, memberId = {}", memberId);
        try {
            memberService.deleteMember(memberId);
            redirectAttributes.addFlashAttribute("successMessage", MemberMessage.SUCCESS_DELETE_MEMBER.getMessage());
        } catch (Exception e) {
            log.warn("ADMIN DELETE deleteMember failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/members";
    }
}
