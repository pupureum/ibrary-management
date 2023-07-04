package com.plee.library.controller.book;

import com.plee.library.annotation.CurrentMember;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.book.request.AddBookRequest;
import com.plee.library.dto.book.request.ReturnBookRequest;
import com.plee.library.dto.book.response.*;
import com.plee.library.service.book.BookService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String allBooks(@PageableDefault(size = 5) Pageable pageable, Model model) {
        log.info("GET allBooks request");
        Page<AllBooksResponse> books = bookService.findAllBooks(pageable);

        model.addAttribute("books", books);
        model.addAttribute("selectedMenu", "book-list");
        return "book/bookList";
    }

    @GetMapping("/{bookId}")
    public String bookDetail(@PathVariable("bookId") Long bookId, Model model, Principal principal) {
        log.info("getBookDetail bookId={}, loginId={}", bookId, principal.getName());
        model.addAttribute("book", bookService.findBookById(bookId));
        model.addAttribute("bookMarked", bookService.isBookMarked(bookId, principal.getName()));
        model.addAttribute("bookLoaned", bookService.isLoaned(bookId, principal.getName()));
        model.addAttribute("selectedMenu", "none");
        return "book/bookDetail";
    }

    //    @GetMapping("/search")
//    public String getSearchBook(Model model) {
//        model.addAttribute("selectedMenu", "none");
//        return "book/searchBookList";
//    }
    @PostMapping("/loan")
    public String loanBook(@RequestParam("bookId") Long bookId, Principal principal, RedirectAttributes redirectAttributes) {
        log.info("loanBook bookId={}, Member={}", bookId, principal.getName());
        try {
            bookService.loanBook(bookId, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "대출이 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            log.warn("loanBook error", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books/loan";
    }

    @GetMapping("/loan")
    public String loanHistory(@PageableDefault(size = 5) Pageable pageable, @CurrentMember Member member, Model model) {
        log.info("/books/loan GET loanHistory request: loginId = {}", member.getName());
        Page<LoanHistoryResponse> loanHistory = bookService.findLoanHistory(member.getId(), pageable);

        model.addAttribute("loanHistory", loanHistory);
        model.addAttribute("selectedMenu", "member-loan-history");
        return "book/loanHistory";
    }

    @PutMapping("/return")
    public String returnBook(@ModelAttribute("returnBookRequest") ReturnBookRequest request, RedirectAttributes redirectAttributes) {
        log.info("returnBook historyId={}", request.getHistoryId());
        log.info("returnBook bookInfo.isbn={}", request.getBookInfoIsbn());
        try {
            bookService.returnBook(request);
            redirectAttributes.addFlashAttribute("successMessage", "반납이 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            log.warn("returnBook error", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books/loan";
    }

    @PutMapping("/renewal")
    public String renewBook(@RequestParam("historyId") Long historyId, RedirectAttributes redirectAttributes) {
        log.info("renewBook historyId={}", historyId);
        try {
            bookService.renewBook(historyId);
            redirectAttributes.addFlashAttribute("successMessage", "연장이 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            log.warn("renewBook error", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books/loan";
    }

    @GetMapping("/api/book")
    @ResponseBody
    public SearchBookResponse searchBooksByApi(@RequestParam("keyword") String keyword) {
        SearchBookResponse response = bookService.findBySearchApi(keyword);
        return response;
    }

    @GetMapping("/request")
    public String requestBookForm(Model model) {
        model.addAttribute("selectedMenu", "member-request-book");
        return "book/request";
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestNewBook(@Valid AddBookRequest request, @CurrentMember Member member) {
        log.info("POST requestNewBook request for member = {}", member.getId());
        bookService.addNewBookRequest(request, member.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/request/history")
    public String requestHistory(@PageableDefault(size = 5) Pageable pageable, @CurrentMember Member member, Model model) {
        log.info("GET requestHistory request for member = {}", member.getId());
        Page<RequestHistoryResponse> response = bookService.findMemberRequestHistory(member.getId(), pageable);

        model.addAttribute("requestHistory", response);
        model.addAttribute("selectedMenu", "member-request-history");
        return "book/requestHistory";
    }

    @PostMapping("/like/{bookId}")
    public String likeBook(@PathVariable Long bookId, Principal principal) {
        log.info("likeBook bookId={}, loginId={}", bookId, principal.getName());
//        Member member = memberService.findByLoginId(principal.getName());
//        System.out.println(member.getRole());
//        memberService.likeBook(member, bookId);
        return "redirect:/book";
    }
}
