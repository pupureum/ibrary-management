package com.plee.library.controller.book;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.dto.book.request.AddBookRequest;
import com.plee.library.dto.book.request.ReturnBookRequest;
import com.plee.library.dto.book.response.AllBooksResponse;
import com.plee.library.dto.book.response.BookInfoResponse;
import com.plee.library.dto.book.response.RequestHistoryResponse;
import com.plee.library.dto.book.response.SearchBookResponse;
import com.plee.library.service.book.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Controller
public class BookController {
    private final BookService bookService;

    @GetMapping("/")
    public String home(Model model) {
        List<BookInfoResponse> response = bookService.findNewBooks();
        model.addAttribute("newBooks", response);
        return "index";
    }

    @GetMapping("/books")
    public String getAllBooks(@PageableDefault(size = 5) Pageable pageable, Model model) {
        Page<AllBooksResponse> books = bookService.findAllBooks(pageable);
        model.addAttribute("books", books);
        model.addAttribute("selectedMenu", "book-list");
        return "book/bookList";
    }

    @GetMapping("/books/{bookId}")
    public String getBookDetail(@PathVariable("bookId") Long bookId, Model model, Principal principal) {
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
    @PostMapping("/books/loan")
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

    @GetMapping("/books/loan")
    public String loanHistory(@PageableDefault(size = 5) Pageable pageable, Model model, Principal principal) {
        log.info("loanHistory loginId={}", principal.getName());
        model.addAttribute("selectedMenu", "member-loan-history");
        model.addAttribute("loanHistory", bookService.findLoanHistory(principal.getName()));
        return "book/loanHistory";
    }

    @PutMapping("/books/return")
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

    @PutMapping("/books/renewal")
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

    @GetMapping("/books/api/book")
    @ResponseBody
    public SearchBookResponse searchBooksByApi(@RequestParam("keyword") String keyword) {
        SearchBookResponse response = bookService.findBySearchApi(keyword);
        return response;
    }

    @GetMapping("/books/request")
    public String requestBookForm(Model model) {
        model.addAttribute("selectedMenu", "member-request-book");
        return "book/request";
    }

    @PostMapping("/books/request")
    public ResponseEntity<String> requestNewBook(@Valid AddBookRequest request, Principal principal) {
        log.info("requestNewBook request={}", request.getReqReason());
        bookService.addNewBookRequest(request, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/books/request/history")
    public String requestHistory(Model model, Principal principal) {
        log.info("member={} request History", principal.getName());
        List<RequestHistoryResponse> response = bookService.findMemberRequestHistory(principal.getName());
        model.addAttribute("requestHistory", response);
        model.addAttribute("selectedMenu", "member-request-history");
        return "book/requestHistory";
    }

    @PostMapping("/books/like/{bookId}")
    public String likeBook(@PathVariable Long bookId, Principal principal) {
        log.info("likeBook bookId={}, loginId={}", bookId, principal.getName());
//        Member member = memberService.findByLoginId(principal.getName());
//        System.out.println(member.getRole());
//        memberService.likeBook(member, bookId);
        return "redirect:/book";
    }

//    @GetMapping("/books/api/search")
//    public String searchBooks(@RequestParam("keyword") String keyword, Model model) {
//        List<BookInfoResponse> books = new ArrayList<>();
//        books.add(BookInfoResponse.builder()
//                        .isbn("1234567890")
//                        .title("테스트 책")
//                        .author("테스트 저자")
//                        .publisher("테스트 출판사")
//                        .image("https://via.placeholder.com/150")
//                        .pubDate("2021-01-01")
//                        .description("테스트 설명")
//                .build());
//        model.addAttribute("books", books);
//        return "admin/addBook";
//    }
}
