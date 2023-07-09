package com.plee.library.controller.admin;

import com.plee.library.dto.admin.request.UpdateBookRequest;
import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.admin.response.AllLoanHistoryResponse;
import com.plee.library.dto.admin.response.LoanStatusResponse;
import com.plee.library.dto.admin.response.RequestStatusResponse;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.admin.response.AllBooksResponse;
import com.plee.library.dto.member.response.MemberInfoResponse;
import com.plee.library.service.book.BookReturnScheduledService;
import com.plee.library.service.book.BookService;
import com.plee.library.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final BookService bookService;

    private final BookReturnScheduledService bookReturnScheduledService;
    private final MemberService memberService;

    @GetMapping("/new-book")
    public String addBookForm(Model model) {
        log.info("ADMIN GET addBookForm request");
        model.addAttribute("selectedMenu", "admin-new-book");
        return "admin/addBook";
    }

    @PostMapping("/new-book")
    public ResponseEntity<String> addBook(@Valid SaveBookRequest request) {
        log.info("ADMIN POST addBook request, book = {}", request.getIsbn());
        try {
            bookService.saveBook(request);
        } catch (Exception e) {
            log.warn("ADMIN POST addBook request failed", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/books")
    public String allBooks(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, Model model) {
        log.info("ADMIN GET allBooks request");
        Page<AllBooksResponse> books = bookService.findAllBooks(pageable);

        model.addAttribute("books", books);
        model.addAttribute("selectedMenu", "admin-book-list");
        return "admin/bookList";
    }

    @PutMapping("/books/{bookId}")
    public String updateBookQuantity(@PathVariable Long bookId, @Valid @ModelAttribute("UpdateBookRequest") UpdateBookRequest request, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("ADMIN PUT updateBookQuantity request, bookId = {}, quantity = {}", bookId, request.getNewQuantity());

        // 유효성 검사 실패시, 에러 메시지를 리다이렉트로 전달
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/books";
        }

        try {
            bookService.updateBookQuantity(bookId, request);
            redirectAttributes.addFlashAttribute("successMessage", "수량이 변경되었습니다.");
        } catch (Exception e) {
            log.warn("ADMIN PUT updateBookQuantity failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @DeleteMapping("/books/{bookId}")
    public String deleteBook(@PathVariable Long bookId, RedirectAttributes redirectAttributes) {
        try {
            log.info("ADMIN DELETE deleteBook request");
            bookService.deleteBook(bookId);
        } catch (Exception e) {
            log.warn("ADMIN DELETE deleteBook failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/books";
    }

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

    @PostMapping("/time")
    public String setReturnTime(@RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time, Model model) {
        log.info("ADMIN POST setReturnTime request, returnTime = {}", time);
//        bookReturnScheduledService.scheduleBookReturn(time);
        return "redirect:/loanStatus";
    }

    @GetMapping("/request")
    public String requestHistory(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, Model model) {
        log.info("ADMIN GET requestHistory request");
        Page<RequestStatusResponse> response = bookService.findAllNewBookReqHistory(pageable);

        model.addAttribute("requestHistory", response);
        model.addAttribute("selectedMenu", "admin-request-status");
        return "admin/requestStatus";
    }

    @GetMapping("/members")
    public String manageMember(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable, Model model) {
        log.info("ADMIN GET manageMember request");
        Page<MemberInfoResponse> response = memberService.findAllMembers(pageable);

        model.addAttribute("members", response);
        model.addAttribute("selectedMenu", "admin-member-management");
        return "admin/memberManagement";
    }

    @PutMapping("/members/{memberId}")
    public ResponseEntity<String> updateMemberInfo(@PathVariable Long memberId, UpdateMemberRequest request) {
        log.info("ADMIN PUT updateMemberInfo request, memberId = {}", memberId);
        try {
            memberService.updateMemberByAdmin(memberId, request);
        } catch (Exception e) {
            log.warn("ADMIN PUT updateMemberInfo failed", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok("Success");
    }

    @DeleteMapping("/members/{memberId}")
    public String deleteMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        log.info("ADMIN DELETE deleteMember request, memberId = {}", memberId);
        try {
            memberService.deleteMember(memberId);
            redirectAttributes.addFlashAttribute("successMessage", "회원이 삭제되었습니다.");
        } catch (Exception e) {
            log.warn("ADMIN DELETE deleteMember failed", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/members";
    }
}
