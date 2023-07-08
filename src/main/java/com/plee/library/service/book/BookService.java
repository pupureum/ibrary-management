package com.plee.library.service.book;

import com.plee.library.domain.book.Book;
import com.plee.library.dto.admin.request.UpdateBookRequest;
import com.plee.library.dto.admin.response.AllBooksResponse;
import com.plee.library.dto.admin.response.AllLoanHistoryResponse;
import com.plee.library.dto.admin.response.RequestStatusResponse;
import com.plee.library.dto.admin.response.LoanStatusResponse;
import com.plee.library.dto.book.request.*;
import com.plee.library.dto.book.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    void saveBook(SaveBookRequest request);

    void loanBook(Long bookId, Long memberId);

    void returnBook(ReturnBookRequest request, Long memberId);

    void renewBook(Long historyId);

    void updateBookQuantity(Long bookId, UpdateBookRequest request);

    void deleteBook(Long bookId);

    void addNewBookRequest(AddBookRequest request, Long memberId);

    void addBookmark(Long memberId, Long bookId);
    void removeBookmark(Long memberId, Long bookId);

    List<BookInfoResponse> findNewBooks();

    Page<AllBooksResponse> findAllBooks(Pageable pageable);

    Page<AllBooksMarkInfoResponse> findAllBooksWithMark(Long memberId, Pageable pageable);

    Page<AllBooksMarkInfoResponse> findBySearchKeyword(SearchBookRequest request, Long memberId, Pageable pageable);

    SearchBookResponse findBySearchApi(String keyword);

    Page<LoanHistoryResponse> findLoanHistory(Long memberId, Pageable pageable);

    Page<RequestHistoryResponse> findMemberRequestHistory(Long memberId, Pageable pageable);

    Page<RequestStatusResponse> findAllNewBookReqHistory(Pageable pageable);

    Page<AllLoanHistoryResponse> findAllLoanHistory(Pageable pageable);

    LoanStatusResponse calculateDailyLoanCounts();

    Book findBookById(Long bookId);

    Page<MarkedBooksResponse> findLikeBooks(Long memberId, Pageable pageable);

    boolean isBookMarked(Long memberId, Long bookId);

    boolean isLoaned(Long memberId, Long bookId);
}