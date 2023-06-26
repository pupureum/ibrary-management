package com.plee.library.controller.book;

import com.plee.library.dto.book.request.AddBookRequest;
import com.plee.library.dto.book.request.SaveBookRequest;
import com.plee.library.dto.book.response.AllBooksResponse;
import com.plee.library.dto.book.response.BookInfoResponse;
import com.plee.library.service.book.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/book")
public class BookController {

    private final BookService bookService;

    @PostMapping("/request")
    public String requestNewBook(@Validated @ModelAttribute("bookInfo") AddBookRequest request, BindingResult bindingResult) {
        // 복합 검증 필요시 적용

        if (bindingResult.hasErrors()) {
            log.info("requestNewBook errors={} ", bindingResult);
            return "member/request";
        }

        bookService.addBookInfo(request, "test@gmail.com");
        return "redirect:/member/requestHistory";
    }

    @GetMapping
    public String getAllBooks(Model model) {
        model.addAttribute("isAdmin", true);
        model.addAttribute("selectedMenu", "book-list");
        List<AllBooksResponse> books = bookService.findAll();
        model.addAttribute("books", books);
        model.addAttribute("totalCount", books.size());
        return "book/bookList";
    }

//    @GetMapping("/api/search")
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


//    @GetMapping
//    public String getBookDetail(Model model) {
//        model.addAttribute("isAdmin", true);
//        model.addAttribute("selectedMenu", "book-list");
//        return "book/bookDetail";
//    }

    @GetMapping("/search")
    public String getSearchBook(Model model) {
        model.addAttribute("isAdmin", true);
        model.addAttribute("selectedMenu", "none");
        return "book/searchBookList";
    }

}
