package com.plee.library.controller.home;

import com.plee.library.dto.book.response.BookInfoResponse;
import com.plee.library.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final BookService bookService;

    @GetMapping("/")
    public String home(Model model) {
        List<BookInfoResponse> response = bookService.findNewBooks();
        model.addAttribute("newBooks", response);
        return "index";
    }
}
