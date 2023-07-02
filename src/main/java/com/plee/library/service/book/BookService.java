package com.plee.library.service.book;

import com.plee.library.domain.book.Book;
import com.plee.library.dto.admin.response.AllBookRequestResponse;
import com.plee.library.dto.book.request.*;
import com.plee.library.dto.book.response.AllBooksResponse;
import com.plee.library.dto.book.response.RequestHistoryResponse;
import com.plee.library.dto.book.response.SearchBookResponse;
import com.plee.library.dto.book.response.LoanHistoryResponse;

import java.util.List;

public interface BookService {
    void saveBook(SaveBookRequest request);

    void loanBook(Long bookId, String loginId);

    void returnBook(ReturnBookRequest request);

    void renewBook(Long historyId);

    void updateBookQuantity(Long bookId, Integer quantity);

    void deleteBook(Long bookId);

    void addNewBookRequest(AddBookRequest request, String loginId);

    List<AllBooksResponse> findAllBooks();

    SearchBookResponse findBySearchApi(String keyword);

    List<LoanHistoryResponse> findLoanHistory(String loginId);

    List<RequestHistoryResponse> findMemberRequestHistory(String loginId);
    List<AllBookRequestResponse> findAllMemberRequestHistory();

    Book findBookById(Long id);

    boolean isBookMarked(Long bookId, String loginId);

    boolean isLoaned(Long bookId, String loginId);
}