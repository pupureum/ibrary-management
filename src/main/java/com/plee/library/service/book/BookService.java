package com.plee.library.service.book;

import com.plee.library.domain.book.Book;
import com.plee.library.dto.admin.response.AllBookRequestResponse;
import com.plee.library.dto.book.request.*;
import com.plee.library.dto.book.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    void saveBook(SaveBookRequest request);

    void loanBook(Long bookId, String loginId);

    void returnBook(ReturnBookRequest request);

    void renewBook(Long historyId);

    void updateBookQuantity(Long bookId, Integer quantity);

    void deleteBook(Long bookId);

    void addNewBookRequest(AddBookRequest request, Long memberId);

    List<BookInfoResponse> findNewBooks();

    Page<AllBooksResponse> findAllBooks(Pageable pageable);

    SearchBookResponse findBySearchApi(String keyword);

    Page<LoanHistoryResponse> findLoanHistory(Long memberId, Pageable pageable);

    Page<RequestHistoryResponse> findMemberRequestHistory(Long memberId, Pageable pageable);

    Page<AllBookRequestResponse> findAllMemberRequestHistory(Pageable pageable);

    Book findBookById(Long id);

    boolean isBookMarked(Long bookId, String loginId);

    boolean isLoaned(Long bookId, String loginId);
}