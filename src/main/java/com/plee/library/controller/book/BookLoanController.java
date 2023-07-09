//package com.plee.library.controller.book;
//
//import com.plee.library.dto.book.request.LoanBookRequest;
//import com.plee.library.dto.book.response.LoanHistoryResponse;
//import com.plee.library.service.book.BookLoanService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.List;
//
//@RequiredArgsConstructor
//@Slf4j
//@Controller
//@RequestMapping("/book")
//public class BookLoanController {
//    private final BookLoanService bookService;
//
////    @PostMapping("/loan")
////    public String loanBook(@RequestParam("bookId") Long bookId, Principal principal) {
////        log.info("loanBook bookId={}, principal={}", bookId, principal.getName());
////        bookService.loanBook(bookId, principal.getName());
////        return "redirect:/book/loan";
////    }
////
////    @GetMapping("/loan")
////    public String loanHistory(Model model, Principal principal) {
////        model.addAttribute("selectedMenu", "member-loan-history");
////        model.addAttribute("loanHistory", bookService.findLoanHistory(principal.getName()));
////        return "member/loanHistory";
////    }
////
////    @PutMapping("/return")
////    public String returnBook(@RequestParam("bookId") Long bookId, Principal principal) {
////        log.info("returnBook bookId={}, principal={}", bookId, principal.getName());
////        bookService.returnBook(bookId, principal.getName());
////        return "redirect:/book/loan";
////    }
////
////    @PutMapping("/renew")
////    public String renewBook(@RequestParam("bookId") Long bookId, Principal principal) {
////        bookService.renewBook(bookId, principal.getName());
////        return "redirect:/book/loan";
////    }
//}
