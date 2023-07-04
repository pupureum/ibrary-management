package com.plee.library.controller.admin;

import com.plee.library.dto.admin.request.UpdateMemberRequest;
import com.plee.library.dto.admin.response.AllBookRequestResponse;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.book.response.AllBooksResponse;
import com.plee.library.dto.member.response.MemberInfoResponse;
import com.plee.library.service.book.BookService;
import com.plee.library.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public String addBookForm(Model model) {
        log.info("ADMIN GET addBookForm request");
        model.addAttribute("selectedMenu", "admin-new-book");
        return "admin/addBook";
    }

    @PostMapping("/new-book")
    public ResponseEntity<String> addBook(@Valid SaveBookRequest request) {
        log.info("ADMIN POST addBook request for book = {}", request.getIsbn());
        bookService.saveBook(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/books")
    public String allBooks(@PageableDefault(size = 10) Pageable pageable, Model model) {
        log.info("ADMIN GET allBooks request");
        Page<AllBooksResponse> books = bookService.findAllBooks(pageable);

        model.addAttribute("books", books);
        model.addAttribute("selectedMenu", "admin-book-list");
        return "admin/bookList";
    }

    @PutMapping("/books/{bookId}")
    public ResponseEntity<String> updateBookQuantity(@PathVariable Long bookId, @RequestParam Integer quantity) {
        log.info("ADMIN PUT updateBookQuantity request for bookId = {}, quantity = {}", bookId, quantity);
        bookService.updateBookQuantity(bookId, quantity);
        return ResponseEntity.ok("Success");
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        log.info("ADMIN DELETE deleteBook request");
        bookService.deleteBook(bookId);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/loan")
    public String loanStatus(@PageableDefault(size = 10) Pageable pageable, Model model) {
        log.info("ADMIN GET loanStatus request");
        model.addAttribute("selectedMenu", "admin-loan-status");
        return "admin/loanStatus";
    }

    @GetMapping("/request")
    public String requestHistory(@PageableDefault(size = 10) Pageable pageable, Model model) {
        log.info("ADMIN GET requestHistory request");
        Page<AllBookRequestResponse> response = bookService.findAllMemberRequestHistory(pageable);

        model.addAttribute("requestHistory", response);
        model.addAttribute("selectedMenu", "admin-request-status");
        return "admin/requestStatus";
    }

    @GetMapping("/members")
    public String manageMember(@PageableDefault(size = 10) Pageable pageable, Model model) {
        log.info("ADMIN GET manageMember request");
        Page<MemberInfoResponse> response = memberService.findAllMembers(pageable);

        model.addAttribute("members", response);
        model.addAttribute("selectedMenu", "admin-member-management");
        return "admin/memberManagement";
    }

    @PutMapping("/members/{memberId}")
    public ResponseEntity<String> updateMemberInfo(@PathVariable Long memberId, UpdateMemberRequest request) {
        log.info("ADMIN PUT updateMemberInfo request for memberId = {}", memberId);
        memberService.updateMemberByAdmin(memberId, request);
        return ResponseEntity.ok("Success");
    }
}
