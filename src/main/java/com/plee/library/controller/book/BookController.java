package com.plee.library.controller.book;

import com.plee.library.annotation.CurrentMember;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.book.request.AddBookRequest;
import com.plee.library.dto.book.request.ReturnBookRequest;
import com.plee.library.dto.book.request.SearchBookRequest;
import com.plee.library.dto.book.response.*;
import com.plee.library.util.message.BookMsg;
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

import java.util.NoSuchElementException;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    // 전체 도서 목록 페이지를 반환합니다.
    @GetMapping
    public String allBooks(@PageableDefault(size = 5, sort = "createdAt", direction = DESC) Pageable pageable,
                           @CurrentMember Member member, Model model) {
        log.info("GET allBooks request");
        Page<AllBooksMarkInfoResponse> response = bookService.findAllBooksWithMark(member.getId(), pageable);

        // 페이징된 도서 정보와 메뉴 정보를 모델에 담아 반환
        model.addAttribute("books", response);
        model.addAttribute("selectedMenu", "book-list");
        return "book/bookList";
    }

    // 도서 상세 뷰를 반환합니다.
    @GetMapping("/{bookId}")
    public String bookDetail(@PathVariable("bookId") Long bookId, Model model, @CurrentMember Member member,
                             RedirectAttributes redirectAttributes) {
        log.info("GEP bookDetail bookId = {}, member = {}", bookId, member.getLoginId());
        try {
            // 도서 상세 정보와 회원의 도서 대출 정보, 찜 여부 정보를 받아 반환
            BookDetailResponse response = bookService.getBookDetails(member.getId(), bookId);
            model.addAttribute("response", response);
        } catch (NoSuchElementException e) {
            // 도서 정보를 찾을 수 없는 경우, 에러 메세지를 담아 리다이렉트
            log.error("bookDetail error", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books";
        }
        return "book/bookDetail";
    }

    // 도서 검색 결과를 반환합니다.
    @GetMapping("/search")
    public String searchBookByKeyword(@Valid @ModelAttribute("searchBookRequest") SearchBookRequest request, BindingResult bindingResult,
                                      @RequestParam("page") int page, @CurrentMember Member member, RedirectAttributes redirectAttributes, Model model) {
        log.info("GET searchBookByKeyword keyword = {}", request.getKeyword());
        if (bindingResult.hasErrors()) {
            log.warn("searchBookByKeyword validation error");
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getFieldError().getDefaultMessage());

            // 유효성 검증 실패 시 검색 시도한 페이지로 리다이렉트하기 위해 page 정보 전달
            redirectAttributes.addAttribute("page", request.getBefore());
            return "redirect:/books";
        }

        // 페이징된 검색 결과를 모델에 담아 반환
        Page<AllBooksMarkInfoResponse> books = bookService.findBySearchKeyword(request, member.getId(), PageRequest.of(page, 5));
        model.addAttribute("books", books);
        return "book/searchBookList";
    }

    // 대출 기록 페이지를 반환합니다.
    @GetMapping("/loan")
    public String loanHistory(@PageableDefault(size = 5, sort = "createdAt", direction = DESC) Pageable pageable, @CurrentMember Member member, Model model) {
        log.info("GET loanHistory member = {}", member.getLoginId());
        Page<LoanHistoryResponse> loanHistory = bookService.findLoanHistory(member.getId(), pageable);

        model.addAttribute("loanHistory", loanHistory);
        model.addAttribute("status", false);
        model.addAttribute("selectedMenu", "member-loan-history");
        return "book/loanHistory";
    }

    // 현재 대출중인 도서 기록을 반환합니다.
    @GetMapping("/on-loan")
    public String onLoanHistory(@CurrentMember Member member, Model model) {
        log.info("GET onLoanHistory member = {}", member.getLoginId());
        Page<LoanHistoryResponse> loanHistory = bookService.findOnLoanHistory(member.getId());

        model.addAttribute("loanHistory", loanHistory);
        model.addAttribute("status", true);
        model.addAttribute("selectedMenu", "member-loan-history");
        return "book/loanHistory";
    }

    // 도서 대출 요청을 처리합니다.
    @PostMapping("/loan")
    public String loanBook(@RequestParam("bookId") Long bookId, @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        log.info("POST loanBook request bookId = {}, loginId = {}", bookId, member.getLoginId());
        System.out.println("here===========");
        try {
            bookService.loanBook(bookId, member.getId());
            redirectAttributes.addFlashAttribute("successMessage", BookMsg.SUCCESS_LOAN_BOOK.getMessage());
        } catch (Exception e) {
            log.warn("loanBook error", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // 대출 요청 처리 후, 대출 기록 페이지로 리다이렉트
        return "redirect:/books/loan";
    }

    // 대출 도서 반납 요청을 처리합니다.
    @PutMapping("/return")
    public String returnBook(@ModelAttribute("returnBookRequest") ReturnBookRequest request, @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        log.info("PUT returnBook historyId = {} book = ", request.getHistoryId(), request.getBookInfoIsbn());
        try {
            bookService.returnBook(request, member.getId());
            redirectAttributes.addFlashAttribute("successMessage", BookMsg.SUCCESS_RETURN_BOOK.getMessage());
        } catch (NoSuchElementException e) {
            log.warn("returnBook error", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // 대출중인 도서만 보기에서 반납처리 한 경우 해당 페이지로 리다이렉트
        if (request.isStatus()) {
            return "redirect:/books/on-loan";
        }
        return "redirect:/books/loan";
    }

    // 대출 도서 연장 요청을 처리합니다.
    @PutMapping("/renewal")
    public String renewBook(@RequestParam("historyId") Long historyId, @RequestParam("status") boolean status, RedirectAttributes redirectAttributes) {
        log.info("PUT renewBook historyId = {}", historyId);
        try {
            bookService.renewBook(historyId);
            redirectAttributes.addFlashAttribute("successMessage", BookMsg.SUCCESS_RENEW_BOOK.getMessage());
        } catch (Exception e) {
            log.warn("renewBook error", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // 대출중인 도서만 보기에서 반납처리 한 경우 해당 페이지로 리다이렉트
        if (status) {
            return "redirect:/books/on-loan";
        }
        return "redirect:/books/loan";
    }

    @GetMapping("/api/book")
    @ResponseBody
    public ResponseEntity<Object> searchBooksByApi(@RequestParam("keyword") String keyword) {
        log.info("GET searchBooksByApi keyword = {}", keyword);
        try {
            // 네이버 검색 api를 사용하여 키워드로 도서 검색
            SearchBookResponse response = bookService.findBySearchApi(keyword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 네이버 서버 에러가 발생한 경우
            log.error("searchBooksByApi error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 신규 도서 요청 뷰를 반환합니다.
    @GetMapping("/request")
    public String requestBookForm(Model model) {
        model.addAttribute("selectedMenu", "member-request-book");
        return "book/request";
    }

    // 신규 도서 요청을 처리합니다.
    @PostMapping("/request")
    public ResponseEntity<String> requestNewBook(@Valid @RequestBody AddBookRequest request, BindingResult bindingResult, @CurrentMember Member member) {
        log.info("POST requestNewBook request member = {}", member.getId());
        // 유효성 검증 실패 시 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            log.warn("POST requestNewBook validation error");
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        try {
            bookService.addNewBookRequest(request, member.getId());
        } catch (Exception e) {
            log.warn("POST requestNewBook error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 도서 요청 기록 페이지를 반환합니다.
    @GetMapping("/request/history")
    public String requestHistory(@PageableDefault(size = 5, sort = "createdAt", direction = DESC) Pageable pageable, @CurrentMember Member member, Model model) {
        log.info("GET requestHistory member = {}", member.getLoginId());
        Page<RequestHistoryResponse> response = bookService.findMemberRequestHistory(member.getId(), pageable);

        model.addAttribute("requestHistory", response);
        model.addAttribute("selectedMenu", "member-request-history");
        return "book/requestHistory";
    }

    // 도서 찜 추가 요청을 처리합니다.
    @PostMapping("/like/{bookId}")
    public String addBookMark(@PathVariable Long bookId, @RequestParam("page") int page, @RequestParam("pageInfo") String pageInfo,
                              @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        log.info("POST likeBook member = {}, bookId = {}", member.getLoginId(), bookId);
        try {
            bookService.addBookmark(member.getId(), bookId);
        } catch (Exception e) {
            log.warn("POST likeBook error", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // 도서 상세 페이지에서 찜 추가 요청 한 경우 상세페이지로 리다이렉트
        if (pageInfo.equals("bookDetail")) {
            return "redirect:/books/" + bookId;
        }

        // 전체 도서 목록으로 리다이렉트
        redirectAttributes.addAttribute("page", page);
        return "redirect:/books";
    }

    // 도서 찜 해제 요청을 처리합니다.
    @DeleteMapping("/unlike/{bookId}")
    public String removeBookmark(@PathVariable Long bookId, @RequestParam("page") int page, @RequestParam("pageInfo") String pageInfo,
                                 @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        log.info("DELETE unlikeBook member = {}, bookId = {}", member.getLoginId(), bookId);
        try {
            bookService.removeBookmark(member.getId(), bookId);
        } catch (Exception e) {
            log.warn("DELETE unlikeBook error", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // 도서 상세 페이지에서 요청이 들어온경우, 도서 상세 페이지로 리다이렉트
        if (pageInfo.equals("bookDetail")) {
            return "redirect:/books/" + bookId;
        }

        // 현재 페이지 번호를 쿼리 파라미터로 추가
        redirectAttributes.addAttribute("page", page);

        // 찜한 도서 페이지에서 요청이 들어온경우, 찜한 도서 페이지로 리다이렉트
        if (pageInfo.equals("bookmarkList")) {
            return "redirect:/books/like";
        }

        // 전체 도서 목록으로 리다이렉트
        return "redirect:/books";
    }

    // 도서 찜 목록 페이지를 반환합니다.
    @GetMapping("/like")
    public String bookmarkList(@PageableDefault(size = 5, sort = "createdAt", direction = DESC) Pageable pageable,
                               @CurrentMember Member member, Model model) {
        log.info("GET likeBookList member = {}", member.getLoginId());
        Page<MarkedBooksResponse> response = bookService.findBookmarked(member.getId(), pageable);

        model.addAttribute("likedBooks", response);
        model.addAttribute("selectedMenu", "liked-books");
        return "book/bookmarkList";
    }
}
