package com.plee.library.controller.admin;

import com.plee.library.dto.admin.response.AllBookRequestResponse;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.member.request.UpdateMemberRequest;
import com.plee.library.dto.member.response.AllMembersResponse;
import com.plee.library.service.book.BookService;
import com.plee.library.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final BookService bookService;
    private final MemberService memberService;

    @GetMapping("/new-book")
    public String addBook(Model model) {
        model.addAttribute("selectedMenu", "admin-new-book");
        return "admin/addBook";
    }

    @PostMapping("/new-book")
    public ResponseEntity<String> addBook(@Valid SaveBookRequest request) {
        bookService.saveBook(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/books")
    public String allBooks(Model model) {
        model.addAttribute("books", bookService.findAllBooks());
        model.addAttribute("selectedMenu", "admin-book-list");
        return "admin/bookList";
    }

    @PutMapping("/books/{bookId}")
    public ResponseEntity<String> updateBookQuantity(@PathVariable Long bookId, @RequestParam Integer quantity) throws Exception {
        log.info("bookId: {}, quantity: {}", bookId, quantity);
        bookService.updateBookQuantity(bookId, quantity);
        return ResponseEntity.ok("Success");
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/loan")
    public String loanStatus(Model model) {
        model.addAttribute("selectedMenu", "admin-loan-status");
        return "admin/loanStatus";
    }

    @GetMapping("/request")
    public String requestHistory(Model model) {
        log.info("admin request History");
        List<AllBookRequestResponse> response = bookService.findAllMemberRequestHistory();
        model.addAttribute("requestHistory", response);
        model.addAttribute("selectedMenu", "admin-request-status");
        return "admin/requestStatus";
    }

    @GetMapping("/members")
    public String manageMember(Model model) {
        List<AllMembersResponse> response = memberService.findAllMembers();
        model.addAttribute("selectedMenu", "admin-member-management");
        model.addAttribute("members", response);
        return "admin/memberManagement";
    }

    @PutMapping("/members/{memberId}")
    public ResponseEntity<String> updateMemberInfo(@PathVariable Long memberId, UpdateMemberRequest request) {
        log.info("update memberId: {}", memberId);
        memberService.updateMemberByAdmin(memberId, request);
        return ResponseEntity.ok("Success");
    }
}
