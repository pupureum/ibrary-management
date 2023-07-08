package com.plee.library.controller.book;

import com.plee.library.annotation.CurrentMember;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.book.request.AddBookRequest;
import com.plee.library.dto.book.request.ReturnBookRequest;
import com.plee.library.dto.book.request.SearchBookRequest;
import com.plee.library.dto.book.response.*;
import com.plee.library.service.book.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String allBooks(@PageableDefault(size = 5, sort = "createdAt", direction = DESC) Pageable pageable, @CurrentMember Member member, Model model) {
        log.info("GET allBooks request");
        Page<AllBooksMarkInfoResponse> books = bookService.findAllBooksWithMark(member.getId(), pageable);

        model.addAttribute("books", books);
        model.addAttribute("selectedMenu", "book-list");
        return "book/bookList";
    }

    @GetMapping("/{bookId}")
    public String bookDetail(@PathVariable("bookId") Long bookId, Model model, @CurrentMember Member member) {
        log.info("GEP bookDetail bookId = {}, member = {}", bookId, member.getLoginId());
        // TODO DTO 로 반환!!!
        model.addAttribute("book", bookService.findBookById(bookId));
        model.addAttribute("bookMarked", bookService.isBookMarked(member.getId(), bookId));
        model.addAttribute("bookLoaned", bookService.isLoaned(member.getId(), bookId));
        model.addAttribute("selectedMenu", "none");
        return "book/bookDetail";
    }

    @GetMapping("/search")
    public String searchBookByKeyword(@Valid @ModelAttribute("searchBookRequest") SearchBookRequest request, BindingResult bindingResult,
                                      @RequestParam("pageNumber") int pageNumber, @CurrentMember Member member,
                                      RedirectAttributes redirectAttributes, Model model) {
        log.info("GET searchBookByKeyword keyword = {}", request.getKeyword());
        if (bindingResult.hasErrors()) {
            log.warn("searchBookByKeyword validation error");
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getFieldError().getDefaultMessage());

            // @Valid 검증 실패 시 검색 시도한 도서 목록 페이지로 리다이렉트하기 위해 page 정보 전달
            redirectAttributes.addAttribute("page", pageNumber);
            return "redirect:/books";
        }

        Page<AllBooksMarkInfoResponse> books = bookService.findBySearchKeyword(request, member.getId(), PageRequest.of(pageNumber, 5));

        model.addAttribute("books", books);
        model.addAttribute("selectedMenu", "none");
        return "book/searchBookList";
    }

    @PostMapping("/loan")
    public String loanBook(@RequestParam("bookId") Long bookId, @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        log.info("POST loanBook request bookId = {}, loginId = {}", bookId, member.getLoginId());
        try {
            bookService.loanBook(bookId, member.getId());
            redirectAttributes.addFlashAttribute("successMessage", "대출이 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            log.warn("loanBook error", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books/loan";
    }

    @GetMapping("/loan")
    public String loanHistory(@PageableDefault(size = 5, sort = "createdAt", direction = DESC) Pageable pageable, @CurrentMember Member member, Model model) {
        log.info("GET loanHistory member = {}", member.getLoginId());
        Page<LoanHistoryResponse> loanHistory = bookService.findLoanHistory(member.getId(), pageable);

        model.addAttribute("loanHistory", loanHistory);
        model.addAttribute("selectedMenu", "member-loan-history");
        return "book/loanHistory";
    }

    @PutMapping("/return")
    public String returnBook(@ModelAttribute("returnBookRequest") ReturnBookRequest request, @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        log.info("PUT returnBook historyId = {} book = ", request.getHistoryId(), request.getBookInfoIsbn());
        try {
            bookService.returnBook(request, member.getId());
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
        log.info("POST requestNewBook request member = {}", member.getId());

        try {
            bookService.addNewBookRequest(request, member.getId());
        } catch (Exception e) {
            log.warn("requestNewBook error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/request/history")
    public String requestHistory(@PageableDefault(size = 5) Pageable pageable, @CurrentMember Member member, Model model) {
        log.info("GET requestHistory member = {}", member.getLoginId());
        Page<RequestHistoryResponse> response = bookService.findMemberRequestHistory(member.getId(), pageable);

        model.addAttribute("requestHistory", response);
        model.addAttribute("selectedMenu", "member-request-history");
        return "book/requestHistory";
    }

    @PostMapping("/like/{bookId}")
    public String addBookMark(@PathVariable Long bookId, @RequestParam("pageNumber") int pageNumber, @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        log.info("POST likeBook member = {}, bookId = {}", member.getLoginId(), bookId);
        try {
            bookService.addBookmark(member.getId(), bookId);
        } catch (Exception e) {
            log.warn("likeBook error", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // 현재 페이지 번호를 쿼리 파라미터로 추가하여 리다이렉트
        redirectAttributes.addAttribute("page", pageNumber);
        return "redirect:/books";
    }

    @DeleteMapping("/unlike/{bookId}")
    public String removeBookmark(@PathVariable Long bookId, @RequestParam("pageNumber") int pageNumber, @RequestParam("pageInfo") String pageInfo,
                             @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        log.info("DELETE unlikeBook member = {}, bookId = {}", member.getLoginId(), bookId);
        try {
            bookService.removeBookmark(member.getId(), bookId);
        } catch (Exception e) {
            log.warn("unlikeBook error", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // 현재 페이지 번호를 쿼리 파라미터로 추가
        redirectAttributes.addAttribute("page", pageNumber);

        // 찜한 도서 페이지에서 요청이 들어온경우, 찜한 도서 페이지로 리다이렉트
        if (pageInfo.equals("bookmarkList")) {
            return "redirect:/books/like";
        }
        return "redirect:/books";
    }

    @GetMapping("/like")
    public String bookmarkList(@PageableDefault(size = 5, sort = "createdAt", direction = DESC) Pageable pageable,
                               @CurrentMember Member member, Model model) {
        log.info("GET likeBookList member = {}", member.getLoginId());
        Page<MarkedBooksResponse> likedBooks = bookService.findLikeBooks(member.getId(), pageable);
        model.addAttribute("likedBooks", likedBooks);
        model.addAttribute("selectedMenu", "liked-books");
        return "book/bookmarkList";
    }
}
