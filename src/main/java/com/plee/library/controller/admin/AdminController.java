package com.plee.library.controller.admin;

import com.plee.library.dto.admin.request.UpdateBookRequest;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.admin.response.AllLoanHistoryResponse;
import com.plee.library.dto.admin.response.LoanStatusResponse;
import com.plee.library.dto.admin.response.RequestStatusResponse;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.admin.response.AllBooksResponse;
import com.plee.library.dto.admin.response.AllMemberInfoResponse;
import com.plee.library.message.BookMsg;
import com.plee.library.message.MemberMsg;
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

import java.util.NoSuchElementException;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {
    private final BookService bookService;

    private final MemberService memberService;

    // 도서 추가 뷰를 반환합니다.
    @GetMapping("/new-book")
    public String addBookForm(Model model) {
        log.info("ADMIN GET addBookForm request");
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
        } catch (IllegalStateException e) {
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
        Page<AllBooksResponse> books = bookService.findAllBooks(pageable);

        model.addAttribute("books", books);
        model.addAttribute("selectedMenu", "admin-book-list");
        return "admin/bookList";
    }

    // 신규 도서 추가 요청을 처리합니다.
    @PutMapping("/books/{bookId}")
    public String updateBookQuantity(@PathVariable Long bookId, @Valid @ModelAttribute("UpdateBookRequest") UpdateBookRequest request,
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
            redirectAttributes.addFlashAttribute("successMessage", BookMsg.SUCCESS_UPDATE_QUANTITY.getMessage());
        } catch (Exception e) {
            log.warn("ADMIN PUT updateBookQuantity failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    // 도서 삭제 요청을 처리합니다.
    @DeleteMapping("/books/{bookId}")
    public String deleteBook(@PathVariable Long bookId, RedirectAttributes redirectAttributes) {
        try {
            log.info("ADMIN DELETE deleteBook request");
            bookService.deleteBook(bookId);
            redirectAttributes.addFlashAttribute("successMessage", BookMsg.SUCCESS_DELETE_BOOK.getMessage());
        } catch (NoSuchElementException e) {
            log.warn("ADMIN DELETE deleteBook failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    // 대출 현황 페이지를 반환합니다.
    @GetMapping("/loan")
    public String loanStatus(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, Model model) {
        log.info("ADMIN GET LoanStatus request");
        // 모든 대출 내역 조회
        Page<AllLoanHistoryResponse> loanHistory = bookService.findAllLoanHistory(pageable);

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
        Page<AllMemberInfoResponse> response = memberService.findAllMembers(pageable);

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
            redirectAttributes.addFlashAttribute("successMessage", MemberMsg.SUCCESS_DELETE_MEMBER.getMessage());
        } catch (Exception e) {
            log.warn("ADMIN DELETE deleteMember failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/members";
    }
}
