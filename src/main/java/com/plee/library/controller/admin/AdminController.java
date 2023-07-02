package com.plee.library.controller.admin;

import com.plee.library.domain.member.Role;
import com.plee.library.dto.admin.response.AllBookRequestResponse;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.book.response.SearchBookResponse;
import com.plee.library.service.book.BookService;
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
    public String manageUserForm(Model model) {
        model.addAttribute("isAdmin", true);
        model.addAttribute("selectedMenu", "admin-member-management");
        model.addAttribute("roleTypes", Role.values());
//        TestUserDto test1 = new TestUserDto("1", "test1", "test1@gmail.com", Role.Member);
//        TestUserDto test2 = new TestUserDto("2", "test2", "test2@gmail.com", Role.Admin);
//        List<TestUserDto> users = List.of(test1, test2);
//        model.addAttribute("users", users);
        return "admin/userManagement";
    }
}
