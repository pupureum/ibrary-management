package com.plee.library.service.book;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.dto.book.request.*;
import com.plee.library.dto.book.response.AllBooksResponse;
import com.plee.library.dto.book.response.SearchBookResponse;
import com.plee.library.dto.book.response.LoanHistoryResponse;

import java.util.List;

public interface BookService {
    void addBookInfo(AddBookRequest request, String loginId);

    void saveBook(SaveBookRequest request);

    void loanBook(Long bookId, String loginId);

    void returnBook(ReturnBookRequest request);

    void renewBook(Long historyId);

    void updateBookQuantity(Long bookId, Integer quantity);

    void deleteBook(Long bookId);

    void addRequest(AddBookRequest request);

    List<AllBooksResponse> findAllBooks();

    SearchBookResponse findBySearchApi(String keyword);

    List<LoanHistoryResponse> findLoanHistory(String loginId);

    Book findBookById(Long id);

    boolean isBookMarked(Long bookId, String loginId);

    boolean isLoaned(Long bookId, String loginId);
}