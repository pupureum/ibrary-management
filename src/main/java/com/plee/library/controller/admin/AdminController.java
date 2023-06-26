package com.plee.library.controller.admin;

import com.plee.library.config.NaverBookSearchConfig;
import com.plee.library.domain.member.Role;
import com.plee.library.dto.TestUserDto;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.book.request.SearchBookRequest;
import com.plee.library.dto.book.response.AllBooksResponse;
import com.plee.library.dto.book.response.BookInfoResponse;
import com.plee.library.dto.book.response.SearchBookResponse;
import com.plee.library.service.book.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final BookService bookService;

    @GetMapping("/new-book")
    public String addBook(Model model) {
        model.addAttribute("isAdmin", true);
        model.addAttribute("selectedMenu", "admin-new-book");
        model.addAttribute("saveBookRequest", new SaveBookRequest());
        return "admin/addBook";
    }


    @PostMapping("/new-book")
    public String addBook(SaveBookRequest request) {
        // 책 정보 처리
        bookService.saveBook(request);
        return "redirect:books";
    }

    //여기가 검색에 걸리는 부분
    @GetMapping("/api/book")
    @ResponseBody
    public SearchBookResponse searchBooks(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        SearchBookRequest searchReq = new SearchBookRequest();
        searchReq.setKeyword(keyword);

        SearchBookResponse response = bookService.findBySearch(searchReq);
        return response;
    }

    @GetMapping("/books")
    public String getAllBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("isAdmin", true);
        model.addAttribute("selectedMenu", "admin-book-list");
        return "admin/bookList";
    }

    @GetMapping("/loan-status")
    public String getLoanStatus(Model model) {
        model.addAttribute("isAdmin", true);
//        model.addAttribute("isAdmin", false);
        model.addAttribute("selectedMenu", "admin-loan-status");
        return "admin/loanStatus";
    }

    @GetMapping("/request-status")
    public String getRequestStatus(Model model) {
        model.addAttribute("isAdmin", true);
//        model.addAttribute("isAdmin", false);
        model.addAttribute("selectedMenu", "admin-request-status");
        return "admin/requestList";
    }

    @PutMapping("/quantity/{bookId}")
    public ResponseEntity<String> updateBookQuantity(@PathVariable Long bookId, @RequestParam Integer quantity) {
        try {
            log.info("bookId: {}, quantity: {}", bookId, quantity);
            bookService.updateBookQuantity(bookId, quantity);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        try {
            bookService.deleteBook(bookId);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/members")
    public String manageUserForm(Model model) {
        model.addAttribute("isAdmin", true);
//        model.addAttribute("isAdmin", false);
        model.addAttribute("selectedMenu", "admin-member-management");
        model.addAttribute("roleTypes", Role.values());
        TestUserDto test1 = new TestUserDto("1","test1", "test1@gmail.com", Role.Member);
        TestUserDto test2 = new TestUserDto("2", "test2", "test2@gmail.com", Role.Admin);
        List<TestUserDto> users = List.of(test1, test2);
        model.addAttribute("users", users);
        return "admin/userManagement";
    }
}
